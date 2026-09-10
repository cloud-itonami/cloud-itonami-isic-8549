(ns training.facts-test
  (:require [clojure.test :refer [deftest is]]
            [training.facts :as facts]))

(deftest jpn-has-a-spec-basis
  (is (some? (facts/spec-basis "JPN")))
  (is (string? (:provenance (facts/spec-basis "JPN")))))

(deftest unknown-jurisdiction-has-no-fabricated-spec-basis
  (is (nil? (facts/spec-basis "ATL"))))

(deftest coverage-never-reports-a-missing-jurisdiction-as-covered
  (let [report (facts/coverage ["JPN" "ATL" "GBR"])]
    (is (= 2 (:covered report)))
    (is (= ["ATL"] (:missing-jurisdictions report)))
    (is (= ["GBR" "JPN"] (:covered-jurisdictions report)))))

(deftest required-evidence-satisfied-needs-every-item
  (let [all (facts/evidence-checklist "JPN")]
    (is (facts/required-evidence-satisfied? "JPN" all))
    (is (not (facts/required-evidence-satisfied? "JPN" (rest all))))
    (is (not (facts/required-evidence-satisfied? "ATL" all)) "no spec-basis -> never satisfied")))

(deftest every-catalog-entry-has-a-distinct-instructor-license-citation
  (doseq [[iso3 entry] facts/catalog]
    (is (string? (:instructor-owner-authority entry)) (str iso3 " instructor-owner-authority"))
    (is (string? (:instructor-legal-basis entry)) (str iso3 " instructor-legal-basis"))
    (is (string? (:instructor-provenance entry)) (str iso3 " instructor-provenance"))))

(deftest can-has-a-spec-basis
  (is (some? (facts/spec-basis "CAN")))
  (is (string? (:provenance (facts/spec-basis "CAN")))))

(deftest can-entry-has-the-same-shape-as-every-other-jurisdiction
  (let [can-keys (set (keys (facts/spec-basis "CAN")))
        jpn-keys (set (keys (facts/spec-basis "JPN")))]
    (is (= jpn-keys can-keys))))

(deftest can-entry-is-honestly-scoped-to-ontario
  ;; Vocational-training regulation in Canada is provincial (no single
  ;; federal registrar) -- the catalog entry represents Ontario
  ;; specifically, not a fabricated pan-Canadian figure, and its own
  ;; :name says so.
  (is (re-find #"(?i)ontario" (:name (facts/spec-basis "CAN")))))

(deftest can-required-evidence-satisfied-needs-every-item
  (let [all (facts/evidence-checklist "CAN")]
    (is (facts/required-evidence-satisfied? "CAN" all))
    (is (not (facts/required-evidence-satisfied? "CAN" (rest all))))))
