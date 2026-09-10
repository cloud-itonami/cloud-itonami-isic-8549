(ns training.registry-test
  (:require [clojure.test :refer [deftest is]]
            [training.registry :as r]))

;; ----------------------------- practice-hours-insufficient? -----------------------------

(deftest not-insufficient-when-meets-minimum
  (is (not (r/practice-hours-insufficient? {:practice-hours-completed 40 :practice-hours-required 30})))
  (is (not (r/practice-hours-insufficient? {:practice-hours-completed 30 :practice-hours-required 30}))))

(deftest insufficient-when-below-minimum
  (is (r/practice-hours-insufficient? {:practice-hours-completed 15 :practice-hours-required 30})))

(deftest insufficient-is-false-on-missing-fields
  (is (not (r/practice-hours-insufficient? {})))
  (is (not (r/practice-hours-insufficient? {:practice-hours-completed 15}))))

;; ----------------------------- register-completion-finalization -----------------------------

(deftest completion-is-a-draft-not-a-real-finalization
  (let [result (r/register-completion-finalization "student-1" "JPN" 0)]
    (is (nil? (get-in result ["certificate" "proof"])))
    (is (= (get-in result ["certificate" "issued_by_registry"]) false))
    (is (= (get-in result ["certificate" "status"]) "draft-unsigned"))))

(deftest completion-assigns-completion-number
  (let [result (r/register-completion-finalization "student-1" "JPN" 7)]
    (is (= (get result "completion_number") "JPN-CMP-000007"))
    (is (= (get-in result ["record" "student_id"]) "student-1"))
    (is (= (get-in result ["record" "kind"]) "completion-finalization-draft"))
    (is (= (get-in result ["record" "immutable"]) true))))

(deftest completion-validation-rules
  (is (thrown? Exception (r/register-completion-finalization "" "JPN" 0)))
  (is (thrown? Exception (r/register-completion-finalization "student-1" "" 0)))
  (is (thrown? Exception (r/register-completion-finalization "student-1" "JPN" -1))))

(deftest history-is-append-only
  (let [c1 (r/register-completion-finalization "student-1" "JPN" 0)
        hist (r/append [] c1)
        c2 (r/register-completion-finalization "student-2" "JPN" 1)
        hist2 (r/append hist c2)]
    (is (= 2 (count hist2)))
    (is (= "JPN-CMP-000000" (get-in hist2 [0 "record_id"])))
    (is (= "JPN-CMP-000001" (get-in hist2 [1 "record_id"])))))
