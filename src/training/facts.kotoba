(ns training.facts
  "Per-jurisdiction other-education-training-provider regulatory
  catalog -- the G2-style spec-basis table the Instruction Integrity
  Governor checks every `:curriculum/verify` proposal against ('did
  the advisor cite an OFFICIAL public source for this jurisdiction's
  training-provider requirements, or did it invent one?'), closely
  modeled on `cloud-itonami-isic-8542`'s `cultural.facts`.

  Coverage is reported HONESTLY (see `coverage`), the same discipline
  every sibling actor's `facts` namespace uses: a jurisdiction not in
  this table has NO spec-basis, full stop -- the advisor must not
  fabricate one, and the governor holds if it tries.

  This blueprint's own named example activities (driving schools,
  exam-preparation courses, corporate training) are genuinely
  heterogeneous -- unlike most sibling `facts` catalogs, there is no
  single unifying regulator across all three. Following the SAME
  'cite the most domain-specific real regulator available' discipline
  `secondary.facts` documented relative to `school.facts`, this
  catalog's GENERAL citation covers training-provider oversight
  broadly, while a SEPARATE driving-instructor-licensing citation
  covers the genuinely new concern this vertical adds: unlike a child-
  performer work permit (`cultural.facts`'s own distinguishing
  concern, which concerns the STUDENT), a driving-school student's
  actual safety depends on whether the INSTRUCTOR who signs off on
  their competency is themselves a licensed driving instructor -- a
  structurally different kind of check from anything else in this
  fleet: verifying the credential of the ASSESSOR, not a fact about
  the subject being assessed.

  \"CAN\" (Canada) is modeled HONESTLY as Ontario specifically, NOT
  as a single pan-Canadian figure -- private career-college /
  vocational-training regulation in Canada is provincial (there is
  no single federal registrar or federal career-college statute),
  the same structural reality `secondary.facts`/sibling catalogs
  disclose for the USA's state-level licensing regime. Ontario was
  picked as one concrete, citable jurisdiction (Ministry of
  Colleges, Universities, Research Excellence and Security /
  Career Colleges Act, 2005) rather than leaving `\"CAN\"` unmodeled;
  this is coverage of Ontario, not a claim of national coverage.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` mirrors the generic
  student-enrollment-consent/curriculum/instructor-license-
  verification/completion-record evidence set; `:legal-basis` /
  `:owner-authority` / `:provenance` are the G2 citation the governor
  requires before any `:curriculum/verify` proposal can commit.
  `:instructor-owner-authority` / `:instructor-legal-basis` /
  `:instructor-provenance` are the SEPARATE driving-instructor-
  licensing citation the governor's `instructor-license-unconfirmed?`
  check is grounded in."
  {"JPN" {:name "Japan"
          :owner-authority "厚生労働省 (Ministry of Health, Labour and Welfare)"
          :legal-basis "職業能力開発促進法 (Act on Human Resources Development Promotion)"
          :national-spec "民間職業訓練施設・教習機関の運営基準"
          :provenance "https://www.mhlw.go.jp/"
          :required-evidence ["受講同意記録 (student-enrollment-consent-record)"
                              "カリキュラム記録 (curriculum-record)"
                              "指導員資格確認記録 (instructor-license-verification-record)"
                              "修了認定記録 (completion-record)"]
          :instructor-owner-authority "都道府県公安委員会 (Prefectural Public Safety Commission), 警察庁 (National Police Agency)"
          :instructor-legal-basis "道路交通法 (Road Traffic Act) -- 指定自動車教習所指導員技能検定"
          :instructor-provenance "https://www.npa.go.jp/policies/application/license_renewal/"}
   "USA" {:name "United States"
          :owner-authority "State Departments of Education / Consumer Affairs (private career/vocational school licensing authority)"
          :legal-basis "State private postsecondary/career school licensing statutes"
          :national-spec "State training-provider registration and disclosure requirements"
          :provenance "https://www2.ed.gov/about/offices/list/ope/ope-office.html"
          :required-evidence ["Student-enrollment-consent record"
                              "Curriculum record"
                              "Instructor-license-verification record"
                              "Completion record"]
          :instructor-owner-authority "State Department of Motor Vehicles (DMV)"
          :instructor-legal-basis "State vehicle-code driving-instructor licensing requirements"
          :instructor-provenance "https://www.nhtsa.gov/road-safety/driver-education"}
   "GBR" {:name "United Kingdom"
          :owner-authority "Office of Qualifications and Examinations Regulation (Ofqual) / Department for Education (DfE)"
          :legal-basis "Apprenticeships, Skills, Children and Learning Act 2009 (training-provider regulation)"
          :national-spec "Training-provider registration and quality-assurance standards"
          :provenance "https://www.gov.uk/government/organisations/ofqual"
          :required-evidence ["Student-enrollment-consent record"
                              "Curriculum record"
                              "Instructor-license-verification record"
                              "Completion record"]
          :instructor-owner-authority "Driver and Vehicle Standards Agency (DVSA)"
          :instructor-legal-basis "Road Traffic Act 1988, Part V (Driving Instruction) -- Approved Driving Instructor (ADI) register"
          :instructor-provenance "https://www.gov.uk/become-a-driving-instructor"}
   "DEU" {:name "Germany"
          :owner-authority "Kultusministerien der Länder (state ministries of education)"
          :legal-basis "Weiterbildungsordnungen der Länder (state further-training-provider regulations)"
          :national-spec "Anerkennungs- und Qualitätsanforderungen für Weiterbildungsträger"
          :provenance "https://www.kmk.org/"
          :required-evidence ["Einwilligungsprotokoll (student-enrollment-consent-record)"
                              "Lehrplanprotokoll (curriculum-record)"
                              "Fahrlehrerlaubnisprüfprotokoll (instructor-license-verification-record)"
                              "Abschlusszertifizierungsprotokoll (completion-record)"]
          :instructor-owner-authority "Fahrerlaubnisbehörden der Länder (regional driving-license authorities)"
          :instructor-legal-basis "Fahrlehrergesetz (FahrlG) -- Fahrlehrerlaubnis (driving instructor license)"
          :instructor-provenance "https://www.gesetze-im-internet.de/fahrlg/"}
   "CAN" {:name "Canada (Ontario)"
          :owner-authority "Ontario Ministry of Colleges, Universities, Research Excellence and Security -- Superintendent of career colleges, appointed under s. 2(1) of the Act"
          :legal-basis "Career Colleges Act, 2005 (Ontario), S.O. 2005, c. 28, Sched. L -- renamed from the Private Career Colleges Act, 2005 effective 2024-01-01; registration required by s. 7(1); operating an unregistered career college is an offence under s. 48(1)(c), punishable under s. 48(2) (individual: fine up to $50,000 and/or up to 1 year imprisonment; corporation: fine up to $250,000)"
          :national-spec "Career college registration (Part IV, ss. 13-22) and the Training Completion Assurance Fund (s. 3(1); premiums/levies under s. 5(1))"
          :provenance "https://www.ontario.ca/laws/statute/05p28"
          :required-evidence ["Student-enrollment-consent record"
                              "Curriculum record"
                              "Instructor-license-verification record"
                              "Completion record"]
          :instructor-owner-authority "Ontario Ministry of Transportation"
          :instructor-legal-basis "Highway Traffic Act, R.S.O. 1990, c. H.8, s. 58(1) -- driving instructor licence requirement; O. Reg. 473/07 (Licences for Driving Instructors and Driving Schools), ss. 2-4 -- prescribed class of driving instruction, authorization, and licence requirements"
          :instructor-provenance "https://www.ontario.ca/laws/regulation/070473"}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to finalize a
  completion record on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-isic-8549 R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog, not a survey of all ~194 "
                 "jurisdictions -- extend `training.facts/catalog`, "
                 "never fabricate a jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings) satisfy
  every evidence item listed for `iso3`? Missing spec-basis -> never
  satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))
