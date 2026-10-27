# 정식 앱 개편 — 로컬 개발 및 검증

기준 HEAD: `9a7da3cdd39692cd39fc153385a5aa85a0a78b25`, 공개 정식 3.1.5/code92.
작업 브랜치: `ui/stable-redesign`. 이번 요청은 분석·구현·검증이며 공개 배포는 실행하지 않는다.

## 계획과 보존 범위

1. 홈: 티어·프로필·남은 5시간/주간 한도 → 이번 주 사용량 → AI의 한마디. 숫자 중심의 간결한 카드, 자세한 설명은 상세로 이동한다.
2. 기록: 월 경계 주 이동 누락 수정. 달력과 작은 화면에서 날짜를 빠짐없이 탐색한다.
3. 성장·도감: 추천 목표와 관측 상태, 보유 요약, 오류/빈 상태/재시도를 구분한다. 티어·희귀도 색과 stable ID를 보존한다.
4. 설정: 기본 설정 즉시 표시, 고급 항목 지연 생성, 업데이트 설정을 직접 제공한다. 계정 전환 중 삭제·내보내기와 백업 작업을 보호한다.
5. 공통: 카드 간격·줄간격·접근성 역할·안전 영역을 정리하고 큰 글꼴과 테마를 검증한다.

DB 형식·성장식·보상·624개 한마디 ID/40팁·OAuth·서명·업데이트 신뢰 주소·위젯/Wear/Windows는 개편의 변경 대상이 아니다. 베타 앱을 새로 만들지 않는다. 기존 테스트/fixture를 덮어쓰지 않는다. 사용량100%가 확인되면 개발을 중단하며 재설정 크레딧을 사용하지 않는다.

## 작업 환경 복구

기존 `.git` 포인터가 삭제된 stable-3.1.1의 Git 저장소를 참조했다. 원격 main을 별도로 읽어 남은2091개 추적 파일을 전부 비교했고 내용 차이가 없었다(줄바꿈 차이 제외). 소스를 checkout/reset하지 않고 Git 저장소와 index만 복원했다. 이전 포인터와 비교 결과는 무시된 `android/build/git-recovery`에 보존한다. SOURCE의 사용자 미커밋 변경과 서명 원본을 수정하지 않는다.

## 검증 기록

- 변경 전 `python android/tests/verify-stable315.py`: 72,762검사 통과. JVM 합성 검증이며 실계정/실기기 증거가 아니다.
- 최종 빌드·회귀·화면 결과는 아래에 실제 실행 후 기록한다.
- Android 공식 [안전 영역 처리](https://developer.android.com/develop/ui/views/layout/edge-to-edge), [접근성 기준](https://developer.android.com/guide/topics/ui/accessibility/apps)을 확인했다. 48dp 조작 영역과 읽기 가능한 글자·선택 상태를 유지한다.

## 구현 결과 — 2026-10-26

| 영역 / 주요 파일 | 변경 이유와 동작 |
|---|---|
| MainActivity / BetaHomeUi / BetaQuotaUi / BetaWeeklyUi | 홈을 티어·프로필·남은 한도 → 주간 사용량 → 한마디 순서로 정리했다. 휘장·이름·티어·레벨·칭호를 압축 배치하고 초기화 절대 시각은 상세에서 제공한다. 주간 막대는 관측한 증가량임을 표시한다. 서버 사용률과 관측 막대를 합산하지 않는다. |
| Ui / BetaUi | 중복 시스템 안전 영역을 제거하고 cutout을 함께 처리한다. 개별 카드 바깥의 중복 둥근 테두리를 제거했다. 카드 간격16dp·본문 줄간격1.2, 설정 행의 버튼 역할·중복 낭독 정리를 적용했다. |
| RecordsActivity / GrowthCalendarNavigation | 10월 마지막 주에서 다음으로 이동할 때11월1일을 건너뛰던 결함을 수정했다. 보이는 날짜 범위의 양 끝에서 하루씩 이동한다. |
| BetaCatalogActivity / BetaCompactGrowthUi | 도감 읽기 오류·마지막 정상 값·재시도·빈 상태를 구분한다. 계정 전환 시 이전 표시를 지우고 장착 계정을 고정한다. 보유 수·달성 막대·선택 상태·좁은 화면1열을 추가했다. 성장 목표 위에는 수집/관측 상태와 필요한 확인 경로를 표시한다. |
| BetaSettingsUi / LedgerAnalyticsActivity | 기본 설정에서 전체 사용량 DB를 읽지 않으며 고급 항목을 최초 클릭 때 생성한다. 자동 업데이트 설정과 현재 상태를 기본 화면에 제공하고 펼침 상태도 전달한다. |
| AccountSession / LedgerAnalyticsActivity / UsageHistoryActivity | 삭제 확인·파일 선택을 시작한 계정에 작업을 묶는다. 잠금 대기 중 계정이 바뀌면 삭제/내보내기를 취소한다. |
| BetaBackupActivity | 선택을 고정하고 준비·파일 선택·읽기·미리보기·저장·복원 진행 상태를 표시한다. 중복 실행을 막고 화면 재생성 중 작업을 이어받는다. 프로세스 종료 후 쓰기·복원은 자동 반복하지 않는다. 설정 전용 복원에는 XP 증감을 표시하지 않는다. |

생산 변경은 기존14파일과 새 날짜 탐색 보조 클래스1개다. 설정·백업 형식, 데이터 키, 성장 경제·보상·카탈로그 ID를 바꾸지 않았다. 내부 Beta 이름의 클래스/저장 키는 정식 데이터의 호환 식별자로 유지하며 별도 베타 앱을 만들지 않았다.

## 실제 실행과 결과

- `python android/tests/verify-stable315.py`: 변경 후에도72,762검사 통과. 기존 계산/한마디/메달/표시/릴리스 파서의 JVM 합성 검증이다.
- `python android/tests/StableRedesignGrowthChecks.py`: 2024~2028년 윤일·월말·연말 연속/역방향 탐색9,137검사 통과.
- `python android/tests/StableRedesignSettingsTest.py`: 생산 AccountSession의 계정 전환·잠금 대기13검사와 설정/SAF 연결8검사 통과.
- `python android/tests/verify-stable-redesign-source.py`: 기준 HEAD 대비 생산 변경 범위와21개 보호 소스, manifest/빌드 식별자/신뢰 주소/카탈로그/lint baseline/기존 테스트 불변 확인.
- `bash android/gradlew -p android :app:assembleRelease :app:lintRelease --no-daemon --console=plain -PreleaseSigningDir=<기존 SOURCE/android/.local-signing>`: 성공. 추가 lint 오류0, 기존 경고281. 기존 baseline으로 제외되는53오류/111경고와 미발견 baseline113항목은 그대로 보고하며 baseline을 갱신하지 않았다.
- `python android/tests/verify-publication.py <APK>`: 공식 폰트·라이선스·패치된 의존성54archive/1APK 통과.
- `apksigner verify --verbose --print-certs <APK>`: 기존 인증서와 v2서명 통과. 패키지 `dev.bennett.codexmeter`, 3.1.5/code92, min26/target36.
- 전용 가상기기 `gpthud_stable315_preview` / `emulator-5586` / API35에서만 설치·화면·합성 데이터 작업을 했다. 사용자 실물 기기를 조작하지 않았다.
- 기존 공개3.1.5 → 로컬 후보를 `adb install -r`로 교체했다. 교체 후42검사, 강제 종료 후 재실행42검사 통과. 같은 UID/최초 설치 시각, 암호화 합성 인증, 레벨/XP/진행률/보상/업적/칭호/장착/수집설정, 원본 보관함, 날짜 메달 전체, 정확한 사용량 관측, 다른 계정과 미래 필드를 보존했다. DB4개 integrity_check 통과. 새 공개 인앱 업데이트/시스템 설치 확인 UI의 검증은 아니다.
- `run-stable-redesign-native.py --serial emulator-5586`: 한국어/다크360×780, 영어/라이트360×780, 한국어/다크320×780·글자2배·긴 이름, 한국어/다크780×360의 네 조합에서 각각238검사 통과. 실제 Locale·텍스트 높이·설정48dp·홈 순서·월 경계·도감 SQLite 오류/재시도·선택 상태·고급 지연 생성·지급 없음 확인. 기존 `Stable315UiNativeTest`도 큰 글씨 조합271검사 통과.
- `--class-name StableRedesignBackupNativeTest`:69검사 통과. 합성 파일의 내보내기/읽기/미리보기·설정 실제 복원, 작업 중 재생성·취소·계정/선택 범위·원본 바이트·중복 방지와 성장/메달/인증 불변 확인. 파일 선택 취소는 계측에서 SAF 요청을 가로채고, 파일 쓰기는 합성 앱 파일로 콜백을 실행했다. 실제 Drive/Documents 제공자의 전체 선택·저장 UI 검증은 아니다.
- 가상기기에 설치된 APK를 다시 꺼내 최종 후보와 SHA256 일치를 확인했다.

### 홈 전후 측정

같은360×780·한국어·다크·기본 글씨·동일 합성 프로필/캐시 실패 상태에서 비교했다.

| 항목 | 기존 공개3.1.5 | 개편 후보 |
|---|---:|---:|
| 툴바 상단 |48dp|24dp|
| 티어·한도 카드 높이 |385dp|303dp|
| 주간 사용량 시작 위치 |650dp|431dp|

자료 없음·조회 실패는 계속 표시한다. 캐시 수치를 정상 최신값으로 표현하거나 빈 관측을0으로 바꾸지 않는다. 큰 글씨/가로에서는 스크롤을 허용한다.

### 실패·수정·미검증

- 첫 빌드 호출은 저장소에 없는 `gradlew.bat` 경로로 실패했다. 실제 기존 `android/gradlew`를 Git Bash로 실행해 빌드했다.
- 첫 기존 native 검사에서 언어 초기 반영이 끝나기 전에 실행돼 실제 Activity 언어 검사가 실패했다. 요청 Locale을 사전 적용·재시작하고 실제 Activity Locale을 다시 검증했다. 실패를 성공으로 바꾸지 않았다.
- 새 보존 소스 검사에서 shared의 ForegroundUpdatePolicy를 app 경로로 지정해 실패했다. 실제 위치를 확인해 shared 경로로 수정했다.
- 백업 계측 최초 시도는 SAF MIME/category 필터 누락, 다음은 그때 남은 외부 파일 선택 화면, 그다음은 AlertDialog 콜백 전 즉시 검사를 수행한 준비 문제로 실패했다. 필터/콜백 동기화를 수정하고 전용 가상기기에서 남은 선택 화면을 취소한 뒤 전체69검사를 통과했다. 실패 로그를 build 아래에 남겼다.
- 과거 버전 번호/정해진 생산 파일 목록을 고정하는 `verify-stable315-source.py`와 전체 과거 fixture 묶음은 이번 새 범위의 성공 근거로 사용하지 않았다. 기존 파일을 삭제/재작성하지 않고 새 기준 검사를 추가했다.
- 실계정 OAuth·실물폰·실사용자 백업 복원·TalkBack 실제 낭독·OEM/장기간 백그라운드·위젯/Wear/Windows 실제 연동은 미검증이다. 기존 연동 소스는 보존했다.

## 로컬 후보와 공개 경계

- 파일: `delivery/stable-redesign/local-20261026/GPT-HUD-3.1.5-code92-redesign-LOCAL.apk`
- 크기:24,808,398bytes. SHA256: `8b6dc672e7b91118b67cc7744768b35d3b00a02567ebdb040c3801eb58bb0592`.
- 기존 서명 인증서 SHA256: `6abd39b13e561cd67fa0d76ab79f9f22500cf5378dc60b176bfe18bf0d2f8be3`.
- 같은 폴더의 SHA256SUMS와 preview에는 실제 합성 가상기기 화면을 보관한다. 일반 사용자 진행 데이터가 아니다.
- 버전 번호를 아직 올리지 않은 로컬 확인용이다. 공개 자동 업데이트로 제공하지 않으며 다음 공개 시 새 version/code가 필요하다.
- 종료 전 공개 latest가 계속v3.1.5/a7bde664이고 공개APK 해시가 기존 `9d8d3a2ca7b0cd8e2e5c33170cee71e8d80032afa538bf69df74b22ac4b39df3`임을 재확인했다.
- push/tag/Release/업로드/Drive 변경·새 서명키·사용자 기기 설치·실사용 데이터 초기화를 실행하지 않았다.
