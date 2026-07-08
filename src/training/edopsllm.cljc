(ns training.edopsllm
  "EdOps-LLM client -- the *contained intelligence node* for the
  other-education-training-provider actor (README: \"EdOps-LLM\"),
  closely modeled on `cloud-itonami-isic-8542`'s `cultural.
  culturaladvisor`.

  It normalizes student intake, drafts a per-jurisdiction training-
  provider evidence checklist, screens students for an unconfirmed
  driving-instructor license (where required), and drafts the
  completion-finalization action. CRITICAL: it is a smart-but-
  untrusted advisor. It returns a *proposal* (with a rationale + the
  fields it cited), never a committed record or a real completion
  finalization. Every output is censored downstream by `training.
  governor` before anything touches the SSoT, and `:actuation/
  finalize-completion` proposals NEVER auto-commit at any phase -- see
  README `Actuation`.

  Like every sibling actor's advisor, this is a deterministic mock so
  the actor graph runs offline and the governor contract is exercised
  end-to-end. In production this calls a real LLM (kotoba-llm or
  equivalent) with the same proposal shape.

  Proposal shape (all kinds):
    {:summary    str            ; human-facing draft / finding
     :rationale  str            ; why -- SCANNED by the spec-basis gate
     :cites      [kw|str ..]    ; facts/sources the LLM used -- SCANNED too
     :effect     kw             ; how a commit would mutate the SSoT
     :stake      kw|nil         ; :actuation/finalize-completion | nil
     :confidence 0..1}"
  (:require #?(:clj  [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [clojure.string :as str]
            [training.facts :as facts]
            [training.registry :as registry]
            [training.store :as store]
            [langchain.model :as model]))

(defn- normalize-intake
  "Directory upsert -- the LLM only normalizes/validates the patch; it
  does not invent the student or jurisdiction. High confidence, low
  stakes."
  [_db {:keys [patch]}]
  {:summary    (str "受講者記録更新: " (pr-str (keys patch)))
   :rationale  "入力 patch の正規化のみ。新規事実の生成なし。"
   :cites      (vec (keys patch))
   :effect     :student/upsert
   :value      patch
   :stake      nil
   :confidence 0.97})

(defn- verify-curriculum
  "Per-jurisdiction training-provider evidence checklist draft.
  `:no-spec?` injects the failure mode we must defend against:
  proposing a checklist for a jurisdiction with NO official spec-basis
  in `training.facts` -- the Instruction Integrity Governor must
  reject this (never invent a jurisdiction's requirements)."
  [db {:keys [subject no-spec?]}]
  (let [s (store/student db subject)
        iso3 (if no-spec? "ATL" (:jurisdiction s))
        sb (facts/spec-basis iso3)]
    (if (nil? sb)
      {:summary    (str iso3 " の公式spec-basisが見つかりません")
       :rationale  "training.facts に未登録の法域。要件を推測で作らない。"
       :cites      []
       :effect     :curriculum/set
       :value      {:jurisdiction iso3 :checklist [] :spec-basis nil}
       :stake      nil
       :confidence 0.9}
      {:summary    (str iso3 " (" (:owner-authority sb) ") 向け必要書類 "
                        (count (:required-evidence sb)) " 件を提案")
       :rationale  (str "公式ソース: " (:provenance sb) " / 法的根拠: " (:legal-basis sb))
       :cites      [(:legal-basis sb) (:provenance sb)]
       :effect     :curriculum/set
       :value      {:jurisdiction iso3
                    :checklist (:required-evidence sb)
                    :spec-basis (:provenance sb)
                    :legal-basis (:legal-basis sb)}
       :stake      nil
       :confidence 0.9})))

(defn- screen-instructor-license
  "Driving-instructor-license screening draft -- the genuinely new
  screening concern this vertical adds. `:instructor-license-
  confirmed? false` on a student whose program itself requires a
  licensed instructor injects the failure mode: the Instruction
  Integrity Governor must HOLD, un-overridably, on any unconfirmed
  instructor license."
  [db {:keys [subject]}]
  (let [s (store/student db subject)]
    (cond
      (nil? s)
      {:summary "対象受講者記録が見つかりません" :rationale "no student record"
       :cites [] :effect :instructor-license-screen/set :value {:student-id subject :verdict :unknown}
       :stake nil :confidence 0.0}

      (not (true? (:instructor-license-required? s)))
      {:summary    (str (:student-name s) ": この課程は指導員資格を要しない -- 審査不要")
       :rationale  "instructor-license-required? が false のため、指導員資格確認要件そのものが発生しない。"
       :cites      [:instructor-license-determination]
       :effect     :instructor-license-screen/set
       :value      {:student-id subject :verdict :not-applicable}
       :stake      nil
       :confidence 0.9}

      (not (true? (:instructor-license-confirmed? s)))
      {:summary    (str (:student-name s) ": 指導員資格が未確認")
       :rationale  "指導員資格を要する課程だが確認状況が未確認。人手確認とホールドが必須。"
       :cites      [:instructor-license-check]
       :effect     :instructor-license-screen/set
       :value      {:student-id subject :verdict :unconfirmed}
       :stake      nil
       :confidence 0.95}

      :else
      {:summary    (str (:student-name s) ": 指導員資格確認済み")
       :rationale  "指導員資格を要する課程、確認済み。"
       :cites      [:instructor-license-check]
       :effect     :instructor-license-screen/set
       :value      {:student-id subject :verdict :confirmed}
       :stake      nil
       :confidence 0.9})))

(defn- propose-completion-finalization
  "Draft the actual COMPLETION-FINALIZATION action -- finalizing a
  real certification or completion record. ALWAYS `:stake :actuation/
  finalize-completion` -- this is a REAL-WORLD act, never a draft the
  actor may auto-run. See README `Actuation`: no phase ever adds this
  op to a phase's `:auto` set (`training.phase`); the governor also
  always escalates on `:actuation/finalize-completion`. Two
  independent layers agree, deliberately."
  [db {:keys [subject]}]
  (let [s (store/student db subject)
        ready? (and s (not (registry/practice-hours-insufficient? s))
                   (or (not (:instructor-license-required? s))
                       (:instructor-license-confirmed? s)))]
    {:summary    (str subject " 向け修了認定提案"
                      (when s (str " (student=" (:student-name s) ")")))
     :rationale  (if s
                   (str "practice-hours-completed=" (:practice-hours-completed s)
                        " practice-hours-required=" (:practice-hours-required s)
                        " instructor-license-required?=" (:instructor-license-required? s)
                        " instructor-license-confirmed?=" (:instructor-license-confirmed? s))
                   "受講者記録が見つかりません")
     :cites      (if s [subject] [])
     :effect     :student/mark-finalized
     :value      {:student-id subject}
     :stake      :actuation/finalize-completion
     :confidence (if ready? 0.9 0.3)}))

(defn infer
  "Route a request to the right proposal generator.
  request: {:op kw :subject id ...op-specific...}"
  [db {:keys [op] :as request}]
  (case op
    :student/intake                    (normalize-intake db request)
    :curriculum/verify                    (verify-curriculum db request)
    :instructor-license/screen               (screen-instructor-license db request)
    :actuation/finalize-completion               (propose-completion-finalization db request)
    {:summary "未対応の操作" :rationale (str op) :cites []
     :effect :noop :stake nil :confidence 0.0}))

;; ----------------------------- Advisor protocol -----------------------------

(defprotocol Advisor
  (-advise [advisor store request] "store + request -> proposal map"))

(defn mock-advisor
  "The deterministic advisor (the `infer` logic above). Default everywhere."
  [] (reify Advisor (-advise [_ st req] (infer st req))))

(def ^:private system-prompt
  (str "あなたは自動車教習所・受験対策講座・企業研修等の修了認定エージェントの助言者です。"
       "与えられた事実のみに基づき、提案を1つだけEDNマップで返します。説明や前置きは"
       "一切書かず、EDNだけを出力します。\n"
       "キー: :summary(人向けドラフト) :rationale(根拠/必ず事実から) "
       ":cites(使った事実キーのベクタ) "
       ":effect(:student/upsert|:curriculum/set|:instructor-license-screen/set|"
       ":student/mark-finalized) "
       ":stake(:actuation/finalize-completion か nil) :confidence(0..1)。\n"
       "重要: 登録されていない法域の要件を絶対に創作してはいけません。"
       "spec-basisが無い場合は :cites を空にし confidence を上げないこと。"
       "指導員資格の確認状況を偽って報告してはいけません。"))

(defn- facts-for [st {:keys [op subject]}]
  (case op
    :curriculum/verify              {:student (store/student st subject)}
    :instructor-license/screen       {:student (store/student st subject)}
    :actuation/finalize-completion   {:student (store/student st subject)}
    {:student (store/student st subject)}))

(defn- parse-proposal
  "Parse the model's EDN proposal defensively. Any parse/shape failure
  yields a safe low-confidence noop so the Instruction Integrity
  Governor escalates/holds -- an LLM hiccup can never auto-finalize a
  completion."
  [content]
  (let [p (try (edn/read-string (str/trim (str content)))
               (catch #?(:clj Exception :cljs :default) _ nil))]
    (if (map? p)
      (-> p
          (update :cites #(vec (or % [])))
          (update :confidence #(if (number? %) (double %) 0.0))
          (update :effect #(or % :noop)))
      {:summary "LLM応答を解釈できませんでした" :rationale (str content)
       :cites [] :effect :noop :stake nil :confidence 0.0})))

(defn llm-advisor
  "An advisor backed by a `langchain.model/ChatModel` (real inference)."
  ([chat-model] (llm-advisor chat-model {}))
  ([chat-model gen-opts]
   (reify Advisor
     (-advise [_ st req]
       (let [msgs [{:role :system :content system-prompt}
                   {:role :user :content (str "操作: " (:op req)
                                              "\n対象: " (:subject req)
                                              "\n事実: " (pr-str (facts-for st req)))}]
             resp (model/-generate chat-model msgs gen-opts)]
         (parse-proposal (:content resp)))))))

(defn trace
  "Decision-grounded audit record -- persisted to the :audit channel."
  [request proposal]
  {:t          :edopsllm-proposal
   :op         (:op request)
   :subject    (:subject request)
   :summary    (:summary proposal)
   :rationale  (:rationale proposal)
   :cites      (:cites proposal)
   :confidence (:confidence proposal)})
