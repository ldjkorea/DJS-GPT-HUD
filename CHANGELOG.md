## GPT HUD 3.1.5 (2026-10-25)

- 검토한 UI 개선을 기존 정식에 통합: 기록 실패/재시도, 날짜 터치 폭·주 단위 보기, 메달 기록 목적지와 도감 빈 상태 수정.
- 홈 잔여 한도·주간 관측 표시, 세 탭 설정 버튼·한마디 ON/OFF·복구 결과·큰 글씨 계정창 개선.
- 정식 package/code92/기존 인증서. 베타 신규 개발·배포 워크플로 종료. 사용자 데이터/과거 복구 이력 보존.

# 3.1.4 — 새 버전 안내와 한마디 보완 (2026-10-24)

- phone3.1.4/code91, 기존 정식 패키지·서명 유지. 사용자 직접 배포 승인에 따라3.1.3과 로컬 한마디 검토본을 잇습니다.
- 자동 확인이 켜져 있으면 앱 시작·복귀 때5분 캐시와 동시 요청 방지로 새 버전을 확인합니다. 버전당 한 번 안내하며 닫은 버전은 재시작 뒤에도 반복하지 않습니다. 오류는 조용히 처리하고 기존 명시적 업데이트 화면으로 연결합니다.
- 한마디131개 수정/493개 유지,624ID·40팁·기존 쿨다운·끄기/숨김·플랜 검증 보존. 일부 한도 미확인을 전체 수집 실패로 단정하지 않습니다. 지원 OS의 중앙 균형 줄바꿈과 큰 글씨 전문을 보존합니다.
- 푸시서버·신규 권한·Drive 업로드·자동 설치 없음. Wear3.1.0/Windows3.1.1과 베타 종료 상태는 유지합니다. 실기기/실계정/시스템 설치 검증은 미실행입니다.
- 실행 결과와 공개 파일은 [3.1.4 검증 기록](docs/GPT_HUD_3.1.4.ko.md)을 따릅니다. 과거 검증 결과와 구분합니다.

# 3.1.3 — 메인 초기화 시각 표시 (2026-10-24)

- 공개 정식3.1.2/e052f88에 초기화 날짜·시각과 남은 시간 표시를 반영합니다. phone3.1.3/code90, 기존 패키지·서명·기록·설정 유지.
- 베타 신규 배포를 종료합니다. 기존 베타 앱·데이터·복구파일을 삭제하거나 자동 이전하지 않습니다.
- 최신 직접 요청으로 미배포 한마디 검토본을 포함합니다. 티어·한도 → 중앙 한마디 → 이번 주간 사용량,200문구 수정/424유지/624안정ID/40팁 보존. 기존 끄기·숨김과 장기 기록 유지.
- 오래된 worker의 이전 플랜 문구 게시와 지난 리셋의 한도 임박 오인을 방지합니다. Wear/Windows 버전 유지.

# 3.1.2 — DJS-GPT-HUD 주소 전환 (2026-10-21)

사용자가 저장소 이름 및 필요한 업데이트 경로 변경·배포를 승인했습니다. GitHub 저장소는 `ldjkorea/DJS-GPT-HUD`(동일 ID1411531926)입니다. 정식 phone3.1.2/code89, Windows3.1.1, Wear3.1.0 유지. 베타9.9.12는 별도 패키지·채널입니다.

**구버전은 처음 한 번 수동 업데이트가 필요합니다.** 정식 APK를 기존 정식 앱 위에 설치하세요. 앱 삭제·데이터 초기화는 필요 없습니다. 새 버전부터 새 주소와 두 정확한 과거 이름을 인식하며, 기존 서명·패키지·체크섬·증가 versionCode 검증을 유지합니다. 비호환 목록 수신 시 기존 업데이트 캐시를 보존합니다.

[변경·검증 범위](docs/DJS_GPT_HUD_MIGRATION.ko.md). 아래는 이전 버전의 당시 기록입니다.

---

# GPT HUD 3.1.1 — 베타9.9.11 정식 전환

2026-10-17 사용자가 검토한 베타9.9.11의 정식3.1.1 구현·검증·공개 배포를 승인했습니다. phone3.1.1/code88,기존 정식 패키지·서명·원본 기록을 유지합니다. Wear/Windows는 정식3.1.0,베타는9.9.11 그대로입니다. 실제 게시 완료와 설치 검증은 배포 영수증이 기준입니다. [변경·검증](docs/GPT_HUD_3.1.1.ko.md).

---

# 9.9.11 BETA — 2026-10-17 (로컬 후보)

- 홈 핵심3영역, 기록 메달 달력, 성장 자동 목표 하나. 기존 상세 기능은 버튼으로 연결.
- 독립 계정별 메달 SQLite 원장, 개인 최근 관측 기준 일간 등급, 주간3/4/5일·월간7/10/14일 메달, 영구 명예3개·칭호2개와 선택 백업 병합.
- 기존600개를 유지하고 메달 반응24개 추가, 기존 실전 팁40개 연결. 한국어 전용 메달 반응이며 영어는 기존 fallback 유지.
- 티어 팔레트 보존, 희귀도·메달 색상/글자/형태 구분. 수동 목표 고정은 추천에 적용하지 않고 옛 설정 원본은 보존.
- phone990011. Wear3.0.0/code85·Windows·정식3.1.0 유지. 공개 배포/Drive 업로드 없음. [검증과 한계](docs/BETA_9.9.11.ko.md).

# GPT HUD BETA 9.9.10 — 주간 표시 안정화

데모 자동 사용량 작업의 샘플 덮어쓰기와 백그라운드 저장 후 홈 갱신 지연을 수정했습니다. 홈은 티어·한도 통합 카드 → 이번 주간 사용량 → AI 한마디 순서입니다. phone9.9.10/code990010, 기존 베타 패키지/서명/데이터를 유지합니다. [변경과 검증](docs/BETA_9.9.10.ko.md). 베타 prerelease/latest=false이며 정식3.1.0은 유지합니다.

---

# 미배포 베타 후속 수정 — 주간 표시 갱신

데모의 자동 사용량 작업이 샘플을15%로 덮어쓰는 문제와 백그라운드 저장 후 홈 갱신 지연을 수정. 데모 예약/실행 차단·전환 중 worker 재확인·화면 snapshot 구독. 공개9.9.9/정식3.1.0은 유지. 상세 재현·반복 검증: docs/BETA_9.9.9.ko.md의 공개 후 수정 절.

# 9.9.9 BETA — 2026-10-17

티어·이름·칭호·레벨과 남은5시간/주간 한도를 한 카드로 통합. 바로 아래 서버 초기화 주기 기준 주간 사용률과 관측 일별 증가량. 홈 지금 할 일/중복 개요 제거. 정식3.1.0과 분리. 상세: docs/BETA_9.9.9.ko.md.

# GPT HUD BETA 9.9.8 — 홈·성장 경로·회고·복구 (베타)

- 홈은 티어·이름·칭호·레벨 박스 → 5시간·주간 남은 한도/잔량 막대/초기화 시간 → 성장·주요 행동 순서로 모든 프리셋에서 표시.
- 기존 목표 고정, 성장 경로와 획득 타임라인, 주간/월간 확인 기록 회고와 선택적 이미지 공유.
- 연결/업데이트/수집 복구 센터, 한국어/영어 진단.
- 성장·설정을 선택하는 백업/미리보기·병합·부분 실패 안내, 파일 선택 중 화면 재생성 복구.
- AI 한마디600개 중150개 개정, 안정 ID600/기존 팁40 유지. 앱의 고정 캐릭터로 자동 선택하며 켜기/끄기만 제공. 편집·추가·수동 교체 및 프롬프트 보관함 없음.
- 성장식/원본XP/획득/장착/보관함·기존 서명·정식3.1.0 보존. beta-v9.9.8 prerelease 채널로 제공.

[구현·실제 검증·미검증](docs/BETA_9.9.8.ko.md).

---

# GPT HUD BETA 9.9.7 — 로그인·업데이트 복구와 다음 목표 (로컬 후보)

- 로그인 취소·재시도, 이전 콜백/다른 계정 선택 후 토큰 반영 차단, 프로세스 종료 후 대기 상태 복구.
- 업데이트 권한 복귀·화면 재생성·중단 후 재시도와 종료 worker 경쟁 차단.
- 성장 화면을 다음 목표·조건·진행도 중심으로 배치하고 홈에는 worker가 만든 목표 요약을 표시.
- 기존 성장 저장 복구, 티어 색상·이름·칭호·레벨, rules4·명예37/칭호24·문구600/팁40 유지.
- 정식3.1.0 보존. 실계정/실기기/실제 다운로드 설치는 미검증. 공개 배포 없음.

[구현·검증·남은 범위](docs/BETA_9.9.7.ko.md).

---

# GPT HUD BETA 9.9.6 — 홈 티어 복구·수집 목표 확장

- 홈에 기존 티어 휘장과 색상을 복구하고 이름·장착 칭호·레벨·경험치를 한 카드에 배치했습니다.
- 명예37개/칭호24개로 확장했습니다. 확인 일수·주간·월간·레벨의 가까운 목표와 장기 목표를 연결했습니다.
- 기존 레벨·XP·획득 날짜·장착·보관함을 보존하며 추가 XP 없이 확인된 기록에서 새 수집품을 계산합니다. 9.9.5 백업도 안전하게 병합합니다.
- 이름·칭호 변경 후 홈 갱신, KO/EN·큰 글씨·가로 화면을 검수했습니다.

[변경·조건·데이터 보존·검증·설치 설명](docs/BETA_9.9.6.ko.md).

---

# GPT HUD BETA 9.9.5 — 개인화·미션·도감 개편

- 기록·홈·성장 3개 탭을 하나의 작은 하단 내비게이션으로 정리했습니다.
- 성장 화면을 내 이름/장착 칭호 → 레벨 → 다음 목표 → 일일·주간·월간 미션 → 수집 메뉴 순서로 구성했습니다.
- 명예 도감은 기본 2열, 큰 글씨에서는 1열입니다. 잠긴 수집품도 서로 다른 실루엣을 보여주며 정확한 조건·진행도·획득 날짜를 확인할 수 있습니다.
- 안정된 칭호 ID에 이름을 대입합니다. 이름과 한국어/영어가 바뀌어도 획득·장착 기록을 유지합니다. 첫 이름·언어 설정은 건너뛸 수 있고 업데이트마다 반복하지 않습니다.
- 설정을 내 정보 / 화면과 언어 / 연결과 알림 / 앱 정보로 묶었습니다. 기존 세부 기능은 고급 설정의 기존 도구에 남겼습니다.
- 프로필 이미지 미리보기, 대표 메달 선택, 직접 선택한 저장/공유, 계정별 성장 백업의 검증 미리보기·안전한 병합 복원을 제공합니다.

새 성장은 사용자가 직접 시작을 확인해야 적용됩니다. 업데이트만으로 실제 기록을 초기화하지 않습니다. [규칙·데이터 보존·검증·설치 설명](docs/BETA_9.9.5.ko.md).

---

# GPT HUD BETA 9.9.4 — 성장 화면 개선

- 수집 상태와 오류·확인 전·저장된 기록을 구분합니다.
- 다음 명예 목표·획득 조건·다음 레벨까지 필요한 MXP를 표시합니다.
- 주간 3/5일, 월간 7/10/14일 보상 지점과 추가 지급량을 안내합니다.
- 미보유 칭호는 획득 조건을 보여주고, 티어·기록·설정 메뉴를 정리합니다.
- 작은 화면 및 큰 글자에서 버튼과 관리 메뉴에 스크롤로 접근할 수 있습니다.
- 기존 베타 패키지·서명·성장 계산·DB 스키마·로그인 복귀 수정을 유지합니다.
- APK 9.9.4/code990004, dev.bennett.codexmeter.trial. 정식 GPT HUD 3.0.0과 별도 채널입니다.

검증: 최종 Release 빌드/lint 성공(경고268개, 기존 baseline 적용), JVM 상태25개, 전용 에뮬레이터 기본/1.6배 글자 각각26개. 성장 경제1533·코치30466 검증, 정식/베타 updater parser12/14개, 서명 및 폰트 출처 검사 통과. 실물 폰·실계정 로그인 완료·실제 폰 업데이트 설치는 미검증입니다. UI 조회·도감 이동은 XP를 지급하지 않습니다.

설치: 기존 베타를 삭제하지 않고 앱의 업데이트 화면에서 새 버전을 확인합니다. 자동 확인이 켜져 있으면 기존 확인 주기에 감지합니다. APK 설치는 Android의 사용자 확인이 필요합니다.

---

# GPT HUD 3.0.0 — 통합 정본

- Android/Wear 3.0.0, versionCode 85. Windows Widget 3.0.0.
- 2.9.2까지의 누적 기능과 2.9.3 개발본의 한국어·영어 선택 및 APK 경량화를 통합했습니다.
- 사용량 원장·달력·결제 결산·XP·티어·업적·칭호·프로필 공유·위젯·PC 연결과 시작 멈춤 수정을 유지합니다.
- 기존 패키지·서명키·DB 및 설정 저장 형식을 유지하며, 설치된 앱을 삭제하거나 데이터를 초기화하지 않고 업데이트합니다.
- 로컬 통합판이며 공개 배포하지 않았습니다. 현재 검증 결과는 `docs/GPT_HUD_3.0.0.ko.md`에 기록합니다.

# GPT HUD 2.9.3 / Windows 0.1.10

## 2.9.3 — 한국어·영어 선택 및 가벼운 설치 파일

- 새 설치는 첫 실행 안내에서 한국어 또는 영어를 선택합니다. 기존 사용자는 기존 화면 언어를 유지합니다.
- 설정 → 화면·위젯에서 앱 언어를 바꾸면 앱 화면과 지원되는 Android 언어 메뉴에 반영됩니다.
- 영어·한국어만 앱 언어로 제공하고, 사용하지 않는 UI 라이브러리 번역을 제외해 휴대폰 APK를 약 2.46MB 줄였습니다.
- 로그인·기록·RPG·결제·위젯 데이터와 기존 앱 서명을 유지합니다.
- Android/Wear 2.9.3/code84. Windows 위젯 0.1.10은 변경하지 않았습니다.
- [2.9.3 검증 보고](docs/GPT_HUD_2.9.3_LANGUAGE.ko.md)

## 2.9.2 — 앱 시작 멈춤 긴급 수정

- 화면의 티어 표시가 백그라운드 사용량 조회 잠금을 기다리지 않도록 수정했습니다.
- 티어 색상 캐시의 교차 잠금 순서를 제거했습니다. DB 복구·이관은 기존 worker에서 수행합니다.
- 로그인·SQLite·XP·메달·칭호·결제·위젯·PC 연결을 보존합니다. 수집 초기화와 계산식 변경은 없습니다.
- Android/Wear 2.9.2/code83, 기존 서명과 패키지 유지. Windows 0.1.10 변경 없음.
- 배포된 2.9.1 격리 APK의 메인 루프 6,001ms 대기를 재현했습니다. 수정 후 네트워크 잠금 중에도 홈 진입과 화면 응답을 확인했습니다. 실제 갤럭시 해결 여부는 미검증입니다.

## 2.9.1 — 안전한 성장 · 내 프로필 이미지 공유

- **내 성취를 한 장으로:** 성장 → 프로필 공유에서 티어·레벨·대표 메달 최대 3개·장착 칭호를 미리 보고 저장하거나 Android 공유 메뉴로 보냅니다. 친구에게 자동 전송하지 않습니다.
- **익명 기본값:** 닉네임은 선택 입력, 생성 날짜는 기본 숨김입니다. 이메일·계정 ID·토큰·결제정보·대화·프로젝트·PC 연결정보를 카드에 포함하지 않습니다. 등급은 GPT HUD의 게임형 평가입니다.
- **계정 전환 보호:** 이전 계정의 성장 화면과 늦은 조회 콜백이 새 계정 화면에 섞이지 않도록 개선했습니다.
- **내 기록 그대로:** XP·레벨·티어·수집 ON/OFF·메달·칭호·사용량·결제·로그인·위젯 설정을 유지합니다. 2.8.51의 일회성 수집 전환을 다시 실행하지 않습니다.
- **기존 업데이트로 설치:** Android/Wear 2.9.1 / versionCode 82, 기존 패키지·서명을 유지합니다. Windows 위젯은 변경 없이 0.1.10과 호환됩니다.

기존 앱의 **기록 → 설정 → 앱 정보 및 라이선스 → 업데이트 → 새 버전 확인**에서 받습니다. 자동 확인이 켜져 있으면 기존 검사 주기에 안내합니다. 설치는 Android의 사용자 확인을 거칩니다. 기존 앱을 삭제하지 마세요.

실제 갤럭시·Samsung 런처/AOD/Now Bar·Wear·실계정 OAuth/API·외부망 Tailscale·장시간 절전·카카오톡/Quick Share 수신은 미검증입니다. 프로필의 실제 클래스/SQLite/PNG/공유 메뉴는 격리 에뮬레이터에서 검증했습니다. 저장 도중 화면이 재생성되면 다시 저장해야 할 수 있습니다.

## 2.8.50 — 데이터 보존 · 화면 정리 · 통합 안정화

- 내부 Phase A~D를 한 버전으로 통합합니다. 2.8.31~49의 개별 공개 Tag·APK는 만들지 않습니다.
- 홈은 주간 한도와 실제 관측 상태를 우선하고 기록은 달력·추세 동선을 정리합니다. 선택적 기능 안내는 로그인·PC 연결을 강요하지 않습니다.
- 같은 정수 부분의 소수값 변화와 미래 PC 관측의 처리를 보완합니다. 서버가 주지 않은 소수점 소비량은 만들지 않습니다.
- 결제 기간별 누적 활용도·이전 결산·계정별 XP·최고 티어를 보존합니다. 기록 공백을 0 활동으로 간주해 강등하지 않습니다.
- 업적·칭호는 각자 시작하고 OFF 기간을 소급하지 않습니다. 이전 자동 메달·대표 업적·장착 칭호·암호화 백업 호환성을 유지합니다.
- 위젯 캐시·스케줄 중복 등록·도감의 같은 데이터 재로딩을 줄입니다. 필터 큰 글꼴·차트 접근성·업데이트 직전 파일 재검증을 보완합니다.
- Windows 0.1.10은 작업·원장 전송의 시각/역순 경계를 보완합니다. 화면·로그인·Tailscale 설정은 유지합니다.
- 기존 package/provider ID·서명·SQLite 스키마를 유지합니다. 실제 삼성/Wear·실계정 OAuth·외부망 장시간 검증은 완료로 주장하지 않습니다.

이전 릴리스 기록은 아래에 보존합니다.

## 2.8.30 — WIDGET SIMPLIFICATION · GROWTH REBORN 통합

- 사용량 2×1과 Codex 작업현황 5×3를 대표 위젯으로 안내합니다. 진화형·삼성 잠금화면은 접힌 고급 선택지로 분류하며 기존 13개 Provider와 설치 ID를 유지합니다.
- 작은 사용량 위젯의 숫자 크기와 조회 실패/저장값 안내 공간을 개선합니다. 기존 소수점·표시 순서·투명도·휘장·탭 동작을 유지하고 설정 저장 시 화면에 없는 필드가 기본값으로 바뀌는 문제를 수정합니다.
- 작업 중인 항목부터 최대 6개를 표시합니다. 연결 실패/오래된 상태는 회색으로 전환하며 명령 오류와 전체 작업 상태를 구별합니다. PC가 보내지 않는 승인·입력 대기는 추정하지 않습니다.
- 변경 없는 PC 확인은 같은 분의 비트맵 재렌더를 생략하고 진화형의 중복 OS 30분 갱신을 제거합니다. 기존 Android 백그라운드 제약은 유지합니다.
- 내부 v2.8.29의 간결한 성장 프로필·독립 업적/칭호 시작·이전 자동 메달 읽기 전용 보관함을 통합합니다. XP·티어·기존 칭호·계정별 암호화 백업·월간 가성비를 보존합니다.
- Windows 위젯은 0.1.9 그대로이며 기존 공개 ZIP을 동일 바이트로 제공합니다. 추가 Windows 재설치나 버전 증가를 요구하지 않습니다.

기존 앱을 삭제하지 않고 업데이트하세요. 실제 삼성 홈/AOD·사용자 계정·PC↔폰 실통신의 검증 여부는 배포 검증 보고서에 별도로 표시합니다.

## 2.8.29 — GROWTH REBORN (내부 개발 · 공개 배포 없음)

- 성장 메인의 네 카드에는 핵심만 표시하고 티어·레벨·요금제 상세를 눌러 확인합니다.
- 업적과 칭호를 독립적으로 시작합니다. 기본 OFF, 확인 후 새 활동만 집계하며 중지/재개는 획득 기록을 지우지 않습니다.
- 이전 자동 메달은 읽기 전용 보관함으로 보존합니다. 기존 칭호·XP·레벨·티어·결제 이력과 계정별 백업은 유지합니다.
- 공식 Pro 100·200·500 비교와 미국 기준 가격·출처·확인 날짜를 저장하고 실제 등록 결제액과 구분합니다.
- 소수점·OAuth·사용량 API·위젯·Windows/Tailscale 구조는 유지하며 Tibo X 자동 수집을 추가하지 않습니다.
- 최신 배포 정책에 따라 v2.8.29 공개 Tag·Release·APK 업로드를 하지 않습니다.


## 2.8.28 — 메달·칭호 스타일 개선 · 세계 티어 문장

- 대표 업적 메달에 이름과 여백을 더해 홈·성장·도감에서 읽기 쉽게 정리했습니다. 칭호 글꼴과 장착 표시도 강화했습니다.
- 기존 53개 업적의 ID·조건·보상은 유지하고 화면에 보이는 이름·소개·조건 요약을 직관적으로 다듬었습니다.
- 대한민국·홈은 삼족오, 세계 추정 티어는 천구·별·월계관 문장으로 구분합니다. 한도 상세 사용량 바는 현재 티어 색상을 따르며 경고 색은 유지합니다.
- Tibo 인용은 저장된 문구임을 표시하고 최신 X 글 열기 버튼을 추가했습니다. 자동으로 X 글을 수집하는 기능은 아닙니다.
- 로그인·SQLite 원장·XP·칭호·대표 업적·위젯·소수점 표시 설정을 유지합니다. Windows 위젯은 기존 0.1.9 그대로입니다.

기존 앱을 삭제하지 않고 업데이트하세요. 기록 → 설정 → 앱 정보 및 라이선스 → 업데이트에서 확인할 수 있습니다. 자동 업데이트 확인이 켜져 있으면 기존 검사에서도 새 버전을 안내합니다. Android 설치 승인은 사용자가 진행합니다.

에뮬레이터의 분리된 QA 계정으로 화면·장착 동작을 검증했습니다. 실제 삼성 휴대폰 설치와 실계정 데이터는 이번에 직접 검증하지 않았습니다.

- 설정 → 위젯·화면 → 사용량 소수점 자리수: 2자리·3자리 선택, 홈·한도 상세·사용량 위젯에 적용. 원본 기록과 계산값 유지.
- PC 위젯 내부 업데이트 확인·다운로드·체크섬 검증·재시작. 기존 로그인·설정·휴대폰 연결 유지.
- Android 버전 검사 및 Windows 표시 버전 불일치 수정.

# GPT HUD 2.8.26 · CODEX 작업 현황 5×3

- 기본 5×3, 2열×2행에서 작업 4개. 높이를 늘리면 6개, 좁으면 1열.
- Windows Widget 0.1.8의 최신 작업 전송 상한 6개. Android 수신·상세 목록·4~6번째 작업 변경 감지 동시 수정.
- 작업 중 녹색, 턴 완료/중단 빨간색, 연결 끊김·오래된 자료·미확인 회색. 수집되지 않는 대기 상태는 추정하지 않음.
- Android 에뮬레이터 실제 RemoteViews 320×180dp의 제목·상태 잘림 수정 및 전용 검사 71개 통과. 실제 갤럭시 런처·PC↔폰 Tailscale 실통신은 미검증.

## 업데이트

폰은 기존 GPT HUD의 업데이트 확인 기능으로 2.8.26을 받거나 휴대폰 APK를 열어 업데이트하세요. 자동 검사/다운로드 이후 설치는 Android의 확인 절차를 따릅니다. 기존 앱을 삭제하지 마세요. 기존 배치 위젯은 런처에 따라 크기를 늘리거나 다시 추가해야 합니다.

PC는 기존 위젯의 ⋯ → 종료 후 Windows 0.1.8 ZIP을 별도 폴더에 풀고 CodexMeterWidget.exe를 실행하세요. 기존 AppData는 삭제하지 마세요. 로그인·기록·Tailscale 연결·화면 설정을 유지합니다. 시작 시 실행을 썼다면 새 폴더에서 껐다 켜 경로를 갱신하세요. 두 기기 모두 수정본을 적용해야 6개 작업을 처리합니다.

이번 배포는 작업현황 위젯 패치입니다. RPG·사용량·인증 엔진은 변경하지 않습니다. 전체 RPG/Wear 회귀검사는 반복하지 않았으며 Release 빌드·작업 전용 검사·서명·공개 다운로드·업데이트 경로를 검증합니다.

---

# GPT HUD 2.8.25 · 성취 보존과 성장 프로필

- 성취 백업·복원: 계정별 XP·업적·칭호·대표 메달·티어·결제 결산을 암호 파일로 저장하고 같은 계정에 복원합니다. 로그인 토큰은 포함하지 않습니다.
- 복원 전 형식·계정·기록을 검사하고 트랜잭션으로 병합합니다. 반복 복원은 보상을 중복 지급하지 않으며 더 최신인 성취를 보존합니다.
- 강등 평가를 실제 갱신 경로에 연결했습니다. 서울 시간·계정 전체 관측·동일 정책의 완전 주간을 확인하지 못하면 강등을 보류합니다.
- 확인된 토큰 이정표에 버전별 1회 XP 보너스를 추가하고, 기존 53개 업적과 별개로 복합 조건의 칭호 10개를 추가했습니다.
- 성장 카드의 고정 높이를 없애고 대표 메달은 가로 3열로 정리했습니다. 도감의 카테고리·정렬은 접어서 보여줍니다.
- 업적 문양을 안정적인 ID에 고정하고 일부 희귀 메달의 날개·일식 문양을 개선했습니다. 기존 기록·위젯·Wear·Windows 연결과 서명은 보존합니다.

기존 앱을 삭제하지 않고 업데이트하세요. 성취 백업은 로그인 후 기록 → 설정 → 성취 백업·복원에서 저장합니다. 백업 암호를 잊으면 복원할 수 없습니다. 같은 OpenAI 계정으로 로그인한 기기에서 가져오세요.

서로 다른 한도 정책·통화는 합산하지 않습니다. 구독료 환산값은 실제 API 청구액·절약액이 아닙니다. 대한민국·세계 티어는 게임형 추정 평가입니다. 기존 서버 자료만으로 완전한 관측 범위를 확인할 수 없으면 강등은 보류합니다.

실제 삼성 갤럭시·개인 계정 로그인/API·사용자 DB 복구는 별도 확인이 필요합니다. Windows Widget 0.1.7은 이번 버전에 변경하지 않습니다.

---

# GPT HUD 2.8.23 · 메달 도감과 칭호 보관함

- 업적 도감을 2열 메달 그리드로 개편했습니다. 상태와 카테고리를 함께 필터링하고 달성 임박·최근 획득·희귀도순으로 탐색하세요.
- 메달을 누르면 조건별 진행 상태, 최초 획득일, 보상 XP와 연결 칭호를 확인합니다. 확인되지 않은 기록은 0으로 단정하지 않습니다.
- 대표 업적은 큰 슬롯 세 개에 직접 장착합니다. 획득한 메달만 선택하며 취소하면 기존 선택을 유지합니다.
- 칭호 보관함은 보유한 칭호부터 보여줍니다. 검색·미리보기·장착·해제를 지원하며 랜덤 추천은 미리보기만 제공합니다.
- 53개 업적을 서로 다른 경량 벡터 문양으로 구분했습니다. 큰 글꼴에서는 도감을 1열로 바꾸고 긴 칭호의 상태 표시 간격을 확보했습니다.
- 기존 업적 ID·달성 조건·XP·티어·대표 업적·장착 칭호·원장과 로그인 저장 구조는 유지합니다. Windows Widget 0.1.7은 변경하지 않았습니다.

## 업데이트

기존 앱에서 **설정 → 업데이트 → 새 버전 확인**을 누르세요. 기존 앱을 삭제하지 않고 업데이트합니다. 자동 확인은 기존 설정과 주기를 따르며 다운로드·검증 후 Android 설치 확인은 직접 눌러야 합니다.

## 검증 범위

전체 회귀 검사, 휴대폰·Wear Release 빌드, lint, 기존 서명과 Pretendard 라이선스를 확인합니다. 업적·칭호 전용 에뮬레이터 762개 검사, 도감 모델 5,523개 검사와 성장 회귀 614개 검사를 통과했습니다. 이 UI 검사는 같은 화면 소스의 독립 QA 패키지에서 수행했습니다. 실제 삼성 갤럭시 설치·실계정 로그인/API·사용자 DB·홈 위젯·TalkBack은 미검증입니다.

---

# GPT HUD 2.8.22 · 눌러보는 성장

- 성장 카드 4개를 균형 있는 2×2 세트로 정리했습니다. 좁은 화면과 큰 글꼴에서는 세로로 배치합니다.
- 대한민국·세계 휘장을 누르면 현재·최고 티어와 확인된 평가 근거, 다음 휘장 안내를 볼 수 있습니다. 게임용 추정 평가이며 실제 순위가 아닙니다.
- RPG 레벨을 누르면 누적 XP, 다음 레벨까지 필요한 XP, 보상별 성장 내역을 확인합니다. 요금제 인장은 중앙에 크게 배치하고 Free·Go·Plus·Pro Lite·Pro 비교를 엽니다.
- 기본 화면에는 대표 업적 최대 3개만 표시합니다. 전체 업적 도감과 칭호 선택을 분리하고 장착 칭호·대표 업적을 홈에도 연결했습니다.
- 기존 41종의 조건과 보상을 유지하고 확인 가능한 활동·토큰·활용 조합 12종을 추가해 업적과 칭호를 각각 53종으로 확장했습니다.
- 업적 종류별 경량 벡터 문양과 새 획득 배너를 추가했습니다. 과거 획득은 다시 연출하지 않으며 효과 OFF·시스템 애니메이션 설정을 존중합니다.
- 성장 진행바·카드 강조색을 티어와 연결했습니다. 기존 로그인·원장·휘장·위젯·PC 연결과 데이터는 유지합니다.

## 업데이트 안내

기존 앱에서 설정 → 업데이트 → 새 버전 확인을 누르세요. 정식 채널로 제공하며 기존 앱을 삭제하지 않고 업데이트합니다. APK 다운로드·검증 후 Android 설치 확인은 직접 눌러야 합니다. 기존 서명과 패키지를 유지합니다.

## 검증 범위

전체 자동 회귀 검사와 휴대폰·Wear Release 빌드·lint·서명·폰트 라이선스를 확인합니다. 성장 UX는 별도 패키지 에뮬레이터에서 628개 검증을 통과했습니다. 실제 삼성 기기·실사용 계정 DB·실 로그인/API·런처 위젯 동작은 검증하지 않았습니다. Windows Widget 0.1.7은 이번에 변경하지 않았습니다.

---

# GPT HUD 2.8.21 · RPG Evolution

## 홈은 간결하게, 성장은 한곳에

- 하단을 **기록 | 홈 | 성장**으로 정리했습니다. 시작 화면은 홈입니다.
- 홈·성장·기존 사용량 위젯이 같은 대한민국 티어를 사용합니다. 세계 티어는 성장에서 별도로 확인합니다.
- 주간 사용량의 기간·잔여율·사용률·초기화 시간·오늘 %p·마지막 조회·상세보기·새로고침을 유지합니다.
- 성장에서 RPG 레벨·XP·현재 요금제 인장, 41개 업적과 41개 칭호를 확인할 수 있습니다. 달성한 칭호를 장착·해제하거나 랜덤 추천받을 수 있습니다.

## 리셋은 기록을 지우지 않습니다

- 실제 등록한 결제 시작일~종료일을 기준으로 누적 활용률과 구독료 환산 활용 금액을 계산합니다.
- 정상 주간 리셋·조기 리셋·확인된 크레딧 사용·요금제 변화·보정을 구별합니다. 같은 관측을 반복해도 누적값을 중복 지급하지 않습니다.
- 남아 있는 원장과 일별 집계에서 확인 가능한 초기화 전 사용량을 복구합니다. 저장되지 않은 소비나 측정 공백은 만들어 넣지 않습니다.
- 지난 결제 기간별 결산을 별도 화면에서 확인합니다. 서로 다른 한도·통화를 합산하지 않습니다. 환산 금액은 절약액이나 실제 API 비용이 아닙니다.

## 삼족오 성취는 계정에 남습니다

- 주간 초기화·전역 초기화·기록 공백만으로 티어가 내려가지 않습니다. 최고 기록을 별도로 보존합니다.
- 2개 유효 주간 저활용은 내부 경고, 3개 연속 저활용과 장기 점수 하락이 함께 확인돼야 강등을 검토합니다. 1회 최대 1단계, 승급 후 2주 보호를 적용합니다.
- 같은 계정의 Pro → Free → Pro 변경에서도 XP·업적·칭호·최고 기록·지난 결산을 보존합니다. 현재 인장과 결제정보는 현재 요금제를 따릅니다.
- Free·Go·Plus·Pro Lite·Pro 인장을 하나의 그래파이트 금속 벡터 계열로 정리했습니다. 상품 구분이 불명확하면 중립 인장을 사용하며 가격을 추정하지 않습니다.

## PC와 연결하면 토큰 활동도 확인

- Windows Widget **0.1.7**에서 공식 Codex 계정 요약과 확인된 루트 세션의 입력·캐시 입력·출력 메타데이터를 읽습니다.
- 기존 인증된 PC 연결을 통해 폰으로 전달합니다. 대화 원문·명령어·인증정보를 전송하지 않습니다. 계정 요약과 로컬 카운터를 합산하지 않습니다.
- 대한민국·세계 등급은 앱의 게임 기준 추정치입니다. 실제 사용자 순위·상위 백분위가 아닙니다.
- 서비스 날짜의 시간대·전체 관측 범위가 확인되지 않아 그 자료만으로 자동 강등하지 않습니다. 검증된 날짜 범위와 충분한 주간 기록이 필요합니다.

## 설치와 확인 범위

기존 GPT HUD를 **삭제하지 않고** APK를 열어 업데이트하세요. 패키지·기존 한국어판 서명을 유지합니다. 기존 위젯·투명도·순서·5시간 숨김·OAuth·원장·알림·Wear·Tailscale 연결을 보존합니다.

자동 테스트와 Android 에뮬레이터의 합성 화면·저장 검증을 수행했습니다. 실제 사용자 갤럭시의 DB 복구액, 설치·로그인·실서버 조회·Samsung 런처/AOD/Now Bar·장시간 절전은 추가 확인이 필요합니다. 앱 삭제 후 자동 클라우드 복원 기능은 없습니다. 기존 폰을 유지한 업데이트를 권장합니다.

# GPT HUD 2.8.20 beta · Windows Widget 0.1.6 beta

- PC Codex 작업 제목을 앱과 작업 현황 위젯에 표시합니다. 대화 원문·명령어·인증 정보는 전송하지 않습니다.
- 명령·도구 오류를 작업 전체 실패와 구분합니다. 턴 완료는 다음 요청 대기로 표시합니다. 승인·입력 대기와 치명적 턴 실패는 미확인입니다.
- 작업 현황 위젯에 현재 확인된 티어의 삼족오·색상·테두리를 적용하고 가로·세로 크기에 맞춰 1~3개 작업을 표시합니다.
- 진화형 위젯 설정 미리보기 오류를 수정하고 작은 크기에서 내용을 모두 숨기는 동작을 없앴습니다.
- Windows Widget 0.1.6이 필요합니다. 기존 로그인·연결·기록·위젯 선택을 유지합니다.

- 상세 화면은 15초 확인, 상태 위젯은 Android 정책에 따른 최선 노력 갱신입니다. 실제 갤럭시·절전·새 버전 외부망 전달은 추가 확인이 필요합니다.

# GPT HUD 2.8.19 beta

## 홈에서 바로 이해하기
- 중앙 삼족오 휘장 아래 영어 티어명을 표시합니다. 미확정 티어는 기록 수집 상태로 유지합니다.
- 주간 기간·사용률·잔여율·초기화 상대 시간과 실제 날짜·오늘 관측 증가량·마지막 조회를 유지합니다. 한도 상세보기와 새로고침도 그대로 사용할 수 있습니다.

## 실제 정밀도에 맞는 숫자
- 원본 소수값을 보존하고 최대 소수점 셋째 자리까지 표시합니다. 정수 응답에 가짜 소수 자릿수를 붙이지 않습니다.
- 기존 티어·가성비·알림 기준과 작은 위젯의 정수 표시는 유지합니다.

## 홈과 기록, 깔끔한 설정
- 하단을 홈·기록으로 정리하고 기록에 분석·캘린더·선택 날짜 요약을 모았습니다.
- 월 이동은 화살표로, 가운데 연월을 누르면 현재 월로 돌아갑니다.
- 표시 설정·관측 기록·변화 이벤트·내보내기를 설정에 모았습니다.

## Codex 작업 현황
- 기능을 켠 경우 홈 주간 카드 아래에서 작업 현황으로 들어갑니다. 기존 PC 연결과 Tailscale을 재사용합니다.
- 별도 GPT HUD · Codex 작업 현황 위젯에 연결 상태와 마지막 확인값을 표시합니다.
- 화면이 열린 동안 15초 확인, 위젯은 Android의 최선 노력 백그라운드 확인입니다. 절전 중에는 지연될 수 있습니다.
- 현재 수집기는 작업 중·턴 완료·중단만 감지합니다. 승인·입력 대기와 실패는 추측하지 않습니다. Windows 0.1.5를 그대로 사용합니다.

## 업데이트 안내
- 기존 앱을 삭제하지 않고 업데이트하세요. 사용 기록·설정·로그인·기존 위젯을 보존합니다. 실제 갤럭시 화면과 새 위젯의 외부 네트워크 확인은 추가 검증이 필요합니다.

## 2.8.18 — Tailscale 외부 작업 상태 연결

- PC·폰 Tailscale 연결에서 밖에서도 작업 상태를 확인합니다. 상세 화면의 기존 15초 확인 주기를 유지합니다.
- PC 연결 코드에 Tailscale 주소를 선택하고 폰의 작업 상태 화면에서 연결을 설정합니다.
- VPN 연결·인증서 고정·연결 코드·동일 계정 확인을 적용합니다. 일반 공인 주소는 허용하지 않습니다.
- 홈 위젯 백그라운드 주기·기존 Wi-Fi 기록 공유·계산·디자인·사용자 데이터는 유지합니다.
- Windows Widget 0.1.5 beta. 실제 LTE·5G 연결은 별도 확인이 필요합니다.

## 2.8.17

- AI의 한마디: 실제 홈 한국어 600개, 상황별 선택, 최근 20개 반복 방지.
- 별도 Codex 작업 현황 시험 위젯(기본 OFF), 같은 Wi-Fi 암호화 연결 재사용.
- 새 턴 시작·완료·중단의 제한적인 로컬 로그 관찰. 승인·입력 대기 및 Remote 감지는 미검증.
- 이름·아이콘·기존 위젯·사용 기록·계산·서명 유지.

## 2.8.16 — GPT HUD · 챌린저 삼족오 앱 아이콘

- 앱 이름을 GPT HUD로 변경하고 챌린저 삼족오 휘장을 Android·Wear 런처 아이콘에 적용합니다.
- 내부 UI·티어 색상·인증·기록·위젯·서명키는 유지합니다.

## Windows Widget 0.1.3 — 티어 색상·장식·앱 아이콘 수정

- Windows에 삼족오 이미지뿐 아니라 휴대폰과 같은 10개 티어의 배경·테두리·게이지·버튼 색과 금속 장식을 연결합니다.
- 실행 파일 아이콘은 삼족오로 바꾸고, 실행 중인 트레이 아이콘은 최신 확인된 티어를 따릅니다. 오래된 값과 미배치는 중립 스타일입니다.
- Windows 실행 파일의 실제 버전 정보를 0.1.3으로 맞춥니다. 이전 창을 닫으면 트레이에 남는 동작과 기존 사용자 데이터는 보존합니다.
- 휴대폰/Wear는 2.8.15 그대로이며 인증·API·원장·LAN 공유·승급 기준은 변경하지 않습니다.

## 2.8.15 — 삼족오 휘장 · 전체 릴리스 기록 · 간결한 설정

- 삼족오 휘장 10종을 홈·분석·휴대폰 홈 위젯·Windows 위젯에 같은 투명 이미지로 적용합니다.
- 홈에서 휘장과 ‘이번 주간 사용량’을 함께 보기 쉽도록 높이·여백을 줄이고, 큰 글꼴에는 세로 배치를 유지합니다.
- Free·Go·Plus·Pro Lite·Pro 요금제 그림을 새로 만들고 Pro Lite/Pro 5x 별칭을 안전하게 구분합니다.
- 1.0.0부터 2.8.15까지 44개 버전의 한국어 설명과 기존 원문을 오프라인으로 보관합니다. 접히는 버전 카드와 줄맞춤된 목록, 설치 버전의 ‘새로운 기능’ 진입점을 제공합니다.
- 진단·개인정보 안내·백업/이전·CSV/JSON·기록 삭제를 일반 설정에서 숨깁니다. 저장소와 기록, 기존 도구 코드는 삭제하지 않습니다.
- Wi-Fi 재접속 시 기존 자동 조회 정책과 마지막 관측 시각을 존중해 화면·위젯을 갱신합니다. 연결된 PC와 공유를 시도하고, 백그라운드 공유는 Android의 네트워크·절전 정책에 따라 주기적으로 재시도합니다.
- 구독 가성비 아래에 X 공식 임베드로 확인한 Tibo의 짧은 문장을 출처·날짜와 함께 표시합니다. 실시간 SNS 수집이나 초기화 판정에는 사용하지 않습니다.
- 인증·사용량 API·원장·구독료·승급 기준·위젯 옵션·기존 서명을 유지합니다. Samsung 실기기 검증은 별도입니다.

## 2.8.14 — Local PC/phone sharing

- Optional pinned-TLS sharing on the same LAN. Merge recent observations and mirror mobile daily archives/tier to the Windows widget.
- Existing OAuth, quota API, widgets and data retained. No cloud service.

# 2.8.13 — 중앙 대형 휘장 · 티어 위젯 배경

- 사용량·초기화 게이지 사이에 티어 휘장을 크게 배치합니다. 모서리 20dp 장식에서 가운데의 주 요소로 바꿉니다.
- 아이언·브론즈·실버·골드·플래티넘·에메랄드·다이아몬드·마스터·그랜드마스터·챌린저별 어두운 금속색 배경과 얇은 테두리를 적용합니다.
- 배경과 게이지 강조색은 현재 확인된 티어를 따릅니다. 배치 중·로그아웃·오래된 데이터에는 확정된 티어 배경을 만들지 않습니다.
- 좁은 위젯은 게이지도 함께 축소해 중앙 휘장과 겹치지 않도록 합니다. 원래 선택한 항목 수와 순서를 유지합니다.
- 미리보기와 홈 위젯에 같은 배경 리소스와 표시 설정을 적용합니다. 휘장 OFF는 원래 테마·강조색, 배경 OFF는 투명입니다.
- 원본 휘장 10종과 로그인·API·SQLite·사용자 기록·서명키를 그대로 유지합니다. 배경 변경은 위젯 내부에 적용하며 휴대폰 바탕화면 배경을 변경하지 않습니다.

Android 2.8.13 / versionCode 43 베타. 실제 삼성 기기의 화면·업데이트 설치는 미검증입니다. 기존 앱을 삭제하지 않고 업데이트하세요.

---

# 2.8.12 — 즉시 활용 티어 · AI의 한마디

- 메인 화면 최상단에 티어 휘장, 아래에 내 사용량 한눈에를 배치했습니다.
- 플랜 활용 티어라는 중복 제목, 평가 이유·금액 계산 설명 버튼, 말투 선택을 제거했습니다.
- AI의 한마디는 일간 관측 변화·주간 사용률에 맞춘 한 가지 매운맛입니다. 기록이 없으면 없는 소비를 지어내지 않습니다.
- 현재 관측을 즉시 누적 활용률에 반영합니다. 주간 마감이나 배치 완료를 기다리지 않습니다.
- 구독 가성비는 직접 등록한 결제액 × 해당 결제 기간의 관측 활용 비율입니다. 실제 절약액·API 비용이 아닙니다.
- 서버 초기화 시각부터 정확한 한도 길이를 사용해 이번 구간을 표시합니다. 누적 소비와 관측한 변화량을 구분합니다.
- 계정 및 구독 오른쪽에 Free·Plus·Pro·Business·Enterprise용 독립적인 로컬 이미지를 추가했습니다.
- 기존 홈 위젯과 티어 진화형 위젯에도 즉시 계산한 티어를 연결했습니다. 숨긴 5시간 한도와 위젯 디자인 설정을 유지합니다.
- 로그인·사용량 API·SQLite·기존 기록·서명키를 보존합니다. Windows Widget 0.1.0은 기존 별도 베타를 사용합니다.

이 릴리스는 Android 2.8.12 / versionCode 42 베타입니다. 실제 삼성 기기 설치·화면·OAuth/API 성공은 검증하지 못했습니다. 기존 한국어판 앱을 삭제하지 않고 업데이트하세요.

---

# 2.8.11 — 기존 홈 위젯 휘장 · Windows 위젯 베타

- 기존 11종 홈 위젯에도 현재 티어 휘장을 표시합니다. 앱을 삭제하거나 위젯을 다시 추가할 필요 없이 갱신 경로에 연결했습니다.
- 위젯 설정의 '티어 휘장 표시'를 제공하고 기존 디자인·색상·순서·투명도·5시간 숨김 설정을 보존합니다. 표시 전에는 흐린 배치 중 휘장으로 구분합니다.
- 별도 Windows 0.1.0 베타 위젯: 이동·크기·항상 위·투명도·글자 크기·선택적 시작 시 실행, 수동/5분 조회.
- PC와 휴대폰의 기록은 자동 동기화되지 않습니다. Windows 실행 파일은 코드 서명이 없고 실계정 로그인·조회는 미검증입니다.
- 기존 Android 로그인·원장·구독료·캘린더·티어 계산·서명키·업데이트 경로는 유지합니다. Samsung/Wear 실기기 검증은 미완료입니다.

# 2.8.10 — 기록 캘린더와 구독료 검증

- 기록 캘린더에서 활동이 두드러진 날짜를 에메랄드빛으로 강조합니다.
- 날짜 선택·오늘 표시·상세 관측 기록·측정 공백 구분을 유지합니다.
- 등록 구독료의 저장·수정·재실행 후 복원과 가성비 계산 반영을 추가 회귀 검사로 확인했습니다.
- 실제 결제액을 한도 기간에 배분한 참고 금액이며, 실제 절약액이나 API 비용을 뜻하지 않습니다.
- 기존 데이터·로그인·서명과 자동 업데이트 경로를 유지합니다.
- Android 실기기 화면 및 설치 검증은 미완료입니다.

## 2.8.9 — Precision Crests

- Replace the active robot/corner-badge presentation with ten bundled transparent, dimensional metal and compute-core emblems on home, analysis and the evolution widget.
- Use 512px WebP assets with verified alpha and bounded decode size; preserve English tier names, evaluation rules, history, account and widget preferences.
- Restore the cached new-release card on home and include updater state in its render signature; retain automatic checks, explicit download/install approval and existing security gates.
- Phone/Wear 2.8.9 / versionCode 39, existing Korean signer. Device/OEM execution remains unverified; distributed as beta.

## 2.8.8

- 장기 주간 티어 평가와 10단계 Meter Scout, 앱 티어 포인트, 별도 1~5요소 홈 위젯.
- 한국어 GitHub 업데이트 저장소·서명 인증서 고정과 높은 versionCode 확인.
- 기존 원장/결제/코치 기록·Pretendard·5시간 숨김·2.8.7 초기화 표시 보존.

## 2.8.7 Korean public beta

- Replace redistributed proprietary font entries with unmodified Pretendard 1.3.9 under SIL OFL 1.1, with verified provenance and complete notices.
- Show the independent 5-hour server reset countdown on home, details and existing widget reset fields; distinguish unavailable, expired, cached and failed refresh states.
- Reuse the minute clock and preserve package/signing/storage/settings contracts; no additional API polling.
- Prepare a separate public snapshot and unsigned, secret-free verification workflow. Device/OEM execution remains unverified.

## 2.8.6 — 2026-10-09

- Coach tones now have distinct local reactions, including short insufficient-data messages and conservative matched-window comparisons.
- Subscription value uses the user-entered tax-inclusive payment allocated to the current quota window; stale or mismatched periods suspend the amount. Arbitrary value rules remain archived and are no longer used by current screens.
- Records now contains calendar, selected-day details, events, three display switches and one Settings entry. Payment, coach and advanced data tools live in a separate settings hub.
- Removed all normal test-note entry points while preserving stored notes, payment history and immutable recaps.
- Existing authentication, ledger, visibility filters, widgets and Wear behavior retained. Phone and Wear version 2.8.6 / 36; physical-device checks remain required.

## 2.8.5 — 2026-10-09

- Introduce a fixed Analysis / Home / Records navigation with Home as the default and retained tab state.
- Apply matte charcoal and white to phone UI while preserving explicit light/system theme choices and saved widget designs.
- Present overview, vector tier emblem, standalone coach quote and same-period used/full play value in the Home scroll.
- Move calendar/date details to Records and organize all settings into six collapsed groups with existing forms and safety confirmations.
- Preserve 2.8.4 billing, tier, coach, notes, immutable recaps, OAuth, ledger and widget/Wear engines.
- Guard asynchronous tab loading against account changes and restore refresh controls after failed unchanged observations.
- Phone and Wear version 2.8.5 / 35; native Galaxy UI and installation checks remain required.

## 2.8.4 — 2026-10-09

- Keep the concise dashboard; move Value to an independent playground with safe legacy links.
- Add device-local quick test notes, durable drafts, status filters and explicit copy/SAF export.
- Add arbitrary play-value rules and full-window subscription allocation with currency/boundary guards.
- Rate utilization from up to four comparable completed windows, with provisional placement for sparse data.
- Include ten original local vector emblems and deterministic local coaches with tone and off settings.
- Preserve payment revisions, play-rule revisions and immutable deduplicated weekly recaps separately from the usage ledger.
- Preserve authentication, API, ledger, widget visibility and reset-credit read-only behavior; no Wear feature expansion.
- Prepare Korean signer-compatible phone/Wear 2.8.4/34; native device verification remains outstanding.

## 2.8.3 — 2026-10-08

- Simplify the dashboard to a usage overview and subscription card, with quota details on demand.
- Refresh on the dashboard with process-wide request deduplication and the existing ledger.
- Unify analytics navigation with Overview, Trends, Records and Value; keep advanced window charts accessible.
- Derive allowance periods from server reset times, compare matching elapsed observations conservatively, and distinguish billing periods.
- Store user-entered subscription amount, currency and billing dates locally; suppress unsupported quota-to-money estimates.
- Honor disabled or unavailable five-hour meters across home/lock widget renders and previews without resetting saved designs.
- Remove Reset Credit execution controls and notification actions while retaining balances, expiry information and alerts.
- Preserve the application ID and Korean signer; phone and Wear versionCode 33. Device verification remains required.

## 2.8.2 — 2026-10-08

- Put remaining allowance, observed daily usage, analytics and manual recording in a prominent dashboard summary.
- Organize analytics into Overview, Trends and Records; improve chart scales, calendar guidance and direct export access.
- Display missing comparison data as unavailable rather than zero; retain observed zero intervals and separate forecast labels.
- Ship the existing local SQLite Usage Ledger and Usage Insights in the Korean update APK.
- Serialize account changes, sign-out and history deletion with in-flight usage requests without blocking the UI.
- Preserve accepted manual saves and exports across activity recreation; isolate stopped maintenance job callbacks.
- Prevent deleted reset-credit events from reimporting and cleared ledgers from rescheduling maintenance.
- Keep the existing application ID and Korean release signing key; versionCode 32 on phone and Wear.
- Local release only. Physical device verification remains required.

## 2.8.1

- Remove the fixed ChatGPT Pro message estimate and history requests.
- Add weekly pace, history-based depletion estimates, observed reset/credit events and data freshness.
- Preserve Korean/English resources, Codex authentication and authoritative usage values.
- Disable in-app release checks and downloads until a Korean release repository is verified.

# Changelog

## 2.8.51 (81) — 2026-10-12

- Restart achievement/title collections once per account at OFF/zero; preserve lifetime XP, tiers, usage and billing, archive prior collections, count only after explicit opt-in.
- Add independent confirmed collection resets and protect them from older archive restores; retain existing IDs and XP award deduplication.
- Put showcase editing in the codex, remove random title recommendation, add tier prerequisites/medal colors for the new collection.
- Use tier-colored quota bars, five calendar bands and whole quota percentages without changing raw precision; remove the latest-X shortcut.
- Add a separate resizable usage + task-status widget using existing stores/authentication/schedulers; retain old providers and Windows 0.1.10 compatibility.

## 2.8.0 — 2026-08-14

### Added

- Free-tier monthly Codex window support: when a paid plan expires to Free, OpenAI's ~30-day monthly limit is parsed and shown across the dashboard, usage history, widgets, Wear, Now Bar, alerts, and adaptive refresh instead of failing with "no recognizable Codex usage data" and leaving a stale Pro badge (#92).
- Opt-in diagnostic log tracing and export (unlock via About header taps) with sanitized JSONL logs for process lifecycle, refresh, OAuth, updates, widgets, Now Bar, and Wear sync (#91).
- Usage-history Customize checklist so chart guide, previous-window list, insight rows, and value estimates can be shown or hidden individually; defaults are decluttered (#89).

### Development

- Monthly-window self-tests cover free-tier parsing, 28/31-day drift, long-window fallbacks, and the downgrade→resubscribe flow; diagnostic sanitizer coverage redacts credentials, emails, JWTs, and OAuth query params (#91, #92).
- Removed the one-time alpha bootstrap workflow now that the `alpha` branch exists, and documented release-channel etiquette for agents (#90).
- Previously shipped to the alpha channel as 2.8.0-alpha.1.

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.7.0...v2.8.0 <!-- pragma: allowlist secret -->

## 2.7.0 — 2026-08-01

### Added

- Alpha release channel with one-tap in-place switching: Settings → Updates → Update channel opts into signed alpha builds, and returning to stable installs in place with no uninstall or data loss because alpha builds share the stable signing key and version code (#88).
- Scrubbable usage-history analytics with per-window insights and plan value estimates (#87).

### Development

- New `alpha` branch (auto-created by CI after merge) feeds `v*-alpha.N` tags; release CI publishes them as GitHub prereleases, never marks them latest, and enforces the shared-versionCode invariant that keeps channel switching downgrade-free (#88).
- Channel-selection self-tests and source guards cover stable/alpha update picks, alpha-to-alpha upgrades, stable promotions, and the return-to-stable flow (#88).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.6.10...v2.7.0 <!-- pragma: allowlist secret -->

## 2.6.10 — 2026-07-31

### Fixed

- Restored dashboard auto-hide for empty usage-credit, reset-credit, and missing-window cards. The 2.6.9 blank-placeholder behavior was intended for widgets only, not the home-screen dashboard (#85).
- Adaptive widget layouts no longer flip to two oversized dials after Spark/additional-limit meters are stripped from saved configs; multi-usage selections migrate to Codex 5-hour + weekly so tall Adaptive sizing keeps progress bars (#85).

### Development

- Shared `resolveVisibleForWidget` / `resolvedSingleUsageMetric` migration helpers cover layout, slot selection, config load, and lock widgets (#85).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.6.9...v2.6.10 <!-- pragma: allowlist secret -->

## 2.6.9 — 2026-07-31

### Changed

- Home and lock widgets no longer offer GPT-5.3-Codex-Spark or other model-specific additional limits; those stay on the in-app dashboard while widgets stick to Codex 5-hour/weekly, next-reset, and reset-credit meters (#84).
- Customize widget meters can be drag-reordered (handle or long-press) so slot priority matches the user’s order, matching Edit dashboard (#84).
- Edit dashboard / Settings visibility switches keep toggled-on sections on screen with a blank dash placeholder when OpenAI has not reported data yet, instead of auto-hiding empty cards inconsistently (#84).

### Fixed

- Selected widget meters that fail to resolve no longer collapse away; they keep a reserved blank dash slot (#84).

### Development

- Regression coverage and static checks updated for the Spark-free widget catalog, dashboard placeholder visibility, and widget meter drag reorder (#84).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.6.8...v2.6.9 <!-- pragma: allowlist secret -->

## 2.6.8 — 2026-07-30

### Added

- Home and Samsung lock widget layout and meter customization: choose Adaptive by size, Dials, or Progress bars, and pick which Codex 5-hour/weekly, Spark (and other additional limits), next-reset, and reset-credits meters fill the available slots (#81).

### Changed

- Reset-credit cards auto-hide when no resets are available (or inventory is unknown), even if the Edit dashboard / Settings toggle is on (#80).
- Usage history stays off the Android dashboard until OpenAI reports a 5-hour or weekly window that can feed a burn chart (#80).
- Usage-credit balances on iOS now hide when zero, near-zero, or negative, matching Android (#80).

### Fixed

- Low-usage alerts no longer re-fire every adaptive refresh when OpenAI's reset timestamp drifts slightly within the same usage window; dedupe uses the same reset-time tolerance as local usage history (#82).

### Development

- Expanded regression coverage for widget meter catalogs, capacity/layout preferences, stale meter fallbacks, reset-credit auto-hide, and low-usage alert dedupe (#80, #81, #82).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.6.7...v2.6.8 <!-- pragma: allowlist secret -->

## 2.6.7 — 2026-07-26

### Fixed

- Reset credits is now a real dashboard section that reorders and hides with the rest of the cards, so the usage-history chart (or any other card) can be placed below it instead of the reset-credit card being pinned to the bottom of the page.
- Reset-credit and usage-credit cards use bold in-card titles with left-aligned icon rows, matching the 5-hour, weekly, and usage-history cards, instead of external One UI section separators that looked inconsistent.

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.6.6...v2.6.7 <!-- pragma: allowlist secret -->

## 2.6.6 — 2026-07-26

### Added

- Visibility switches on every row of the Edit dashboard screen so usage cards can be hidden or shown while rearranging them, including per-limit control over model-specific limits.
- Usage history is now its own dashboard section that can be reordered and hidden like the other usage cards, on the dashboard, in the edit screen, and in Settings → Refresh & usage.

### Changed

- 5-hour and weekly burn charts hide from the usage-history card and the full history screen while OpenAI has not yet reported those windows, replacing blank "Waiting for usage data" graphs with a short hint.
- Dashboard visibility and hidden-section choices are included in settings transfer exports.

### Fixed

- Restored the One UI design for the reset-credit and usage-credit sections (left-aligned Separator + CardItemView rows with icons) that regressed when the dashboard-reordering branch superseded the earlier cleanup.

### Development

- Expanded dashboard-section regression coverage for the usage-history key and hidden-section CSV round-trips.

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.6.5...v2.6.6 <!-- pragma: allowlist secret -->

## 2.6.5 — 2026-07-26

### Added

- Local usage history with burn charts and trend-aware pace estimates on Android (#68).
- Responsive single-dial home and lock widgets that adapt to available size, with clarified Both windows / 5-hour only / Weekly only allowance labels (#68).
- Samsung One UI Watch modular tile metadata (2×2 overview and 2×1 focused tiles), redesigned Wear tiles with One UI Sans and battery-style dials, and adaptive complications across short/long text, ranged/goal progress, and image slots (#67).
- Drag-to-reorder dashboard sections for the 5-hour limit, weekly limit, additional model limits, and usage-credit balance, with a One UI edit screen and Settings entry (#75).

### Changed

- Usage-credit balance cards auto-hide when the balance is empty, effectively zero, negative, or absent (#75).
- Widget allowance configuration uses shared One UI choice dialogs on phone and lock widgets (#68).

### Development

- Expanded regression coverage for usage-credit auto-hide, dashboard section order merging, Wear modular tile metadata, and widget configuration wiring (#67, #68, #75).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.6.0...v2.6.5 <!-- pragma: allowlist secret -->

## 2.6.0 — 2026-07-26

### Added

- Adaptive Automatic refresh mode (now the default) on Android and iOS that adjusts cadence from remaining quota, reset proximity, recent engagement, quiet hours, accelerated usage, and failure backoff (#71).
- Adaptive usage dashboard sections that preserve and render named additional rate limits such as GPT-5.3-Codex-Spark, hide missing standard windows instead of empty cards, and show purchased usage-credit balances separately from reset credits (#70).
- Dashboard visibility controls on Android and iOS so users choose which usage sections appear, with safe migration defaults (#70).

### Changed

- Bumped the phone app One UI design library to `0.9.14+oneui8` and refreshed `android/vendor/m2` so offline and release builds resolve the new AAR without GitHub Packages auth (#69).
- Wear freshness now follows the phone's effective adaptive refresh cadence (#71).

### Fixed

- One UI `CardItemView` / `SwitchItemView` RTL summary `ViewStub` inflation via the design-library update (#69).

### Development

- Expanded regression coverage for adaptive refresh policy and settings migration, plus adaptive usage parsing for partial, additional-only, and credit-only responses (#70, #71).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.5.0...v2.6.0 <!-- pragma: allowlist secret -->

## 2.5.0 — 2026-07-20

### Added

- Fully supported Wear OS companion distribution with a signed `CodexMeter-Wear-2.5.0.apk` attached directly to tagged GitHub Releases alongside the phone APK and shared SHA-256 checksums.
- Phone-owned Wear sync status for sign-in, refresh progress and errors, initial state hydration, plan and limit state, reset credits, stale data, and accelerated-usage settings.
- Wear-native overview, five-hour, weekly, reset, and monitor Tiles; five-hour, weekly, dual-usage, and next-reset Complications; and promoted Ongoing Activity / Live Update monitoring.

### Changed

- Rebuilt the Wear application and settings around the Android app's One UI dark design system, including matching typography and colors, 28dp cards, true pill controls, stateful touch feedback, One UI settings rows, rounded popup surfaces, and explicitly styled switches.
- Replaced the generated Wear launcher artwork with the canonical Android adaptive icon layers and official Codex notification mark.
- Updated the Wear dependency stack and compile platform for current Wear OS releases while retaining the Android 16 target behavior.
- Exhausted Now Bar and Wear monitor windows show their reset countdown instead of an unhelpful `0%`.

### Fixed

- Signing out now clears cached usage and active monitor state from the watch instead of leaving sensitive stale data visible.
- Wear refresh requests return useful progress and failure state, and opening the app hydrates existing Data Layer items instead of waiting indefinitely for another phone update.
- Removed release-accessible fake demo usage that could be mistaken for real synchronized account data.
- Tagged GitHub Releases now publish the Wear APK that CI already built and verified, rather than listing it only inside `SHA256SUMS.txt`.

### Development

- Added regression coverage for Wear sync clear/status payloads, pace settings, stale/account formatting, canonical icon parity, and public Wear release-asset publication.
- Validated the signed app, all five Tiles, all four Complications, One UI controls, and promoted ongoing notification on a Wear OS 6.1 large-round emulator.

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.4.3...v2.5.0 <!-- pragma: allowlist secret -->

## 2.4.3 — 2026-07-19

### Fixed

- Android Appearance settings once again show the illustrated Light and Dark theme options side by side, while preserving System default behavior in the redesigned settings navigation.

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.4.2...v2.4.3 <!-- pragma: allowlist secret -->

## 2.4.2 — 2026-07-19

### Added

- Off option for accelerated-usage warning sensitivity so pace estimates can stay on without orange warnings or matching Now Bar auto-start triggers (#57).
- Reset-credit expiration details on the dashboard and inventory, with soonest-expiring credit selection when redeeming (#56).

### Changed

- Android Settings now uses a compact hub with focused destinations for appearance, refresh and usage, notifications, Now Bar, updates, transfer, and privacy, revealing nested options only when their parent feature is enabled (#54).

### Fixed

- Onboarding card spacing and Settings bottom padding no longer collapse against adjacent cards or the screen edge (#52).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.4.1...v2.4.2 <!-- pragma: allowlist secret -->

## 2.4.1 — 2026-07-18

### Fixed

- Active Android Live Updates now rebuild immediately after promoted-notification access changes, allowing automatic Samsung fallback and explicit Android modes to adopt the newly available notification contract without waiting for another usage refresh.
- Live Update construction now adapts to both early Android 16's required colorization and newer uncolorized promotion rules, while notification and Now Bar Codex icons use SystemUI-compatible vector syntax.

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.4.0...v2.4.1 <!-- pragma: allowlist secret -->

## 2.4.0 — 2026-07-18

### Added

- Wear OS companion app with phone↔watch usage and settings sync, plus tiles, complications, and Ongoing Activity monitoring (#37).
- Native iOS / iPadOS SwiftUI + WidgetKit client under `ios/`, with portable meters, reset credits, notifications, widgets, and Keychain-backed auth (#22).
- Android settings transfer: export and import selected sections (app settings, notifications, Now Bar, and ChatGPT authentication) from Settings, with explicit warnings when authentication tokens are included (#42).
- Configurable automatic update-check frequency (hourly through weekly) and optional notifications when a newer signed GitHub release is available (#38).
- Estimated usage-pace projections on dashboard cards, with One UI functional-orange accelerated-usage warnings and matching Now Bar auto-start / regress triggers (#47).
- Widget Background toggle in customize settings so the fill can be turned off entirely, matching official One UI widgets (#45).
- Optional Material You accent switch under Settings → Appearance so app accents can follow the system palette on Android 12+ (#44).

### Changed

- Default theme primary now uses Samsung's official One UI Primary (`#0381FE`) instead of the lighter SESL sample blue (#44).
- Widget opacity control now uses three discrete fill strengths (56% / 88% / 100%) instead of four, aligning with current Samsung One UI widget transparency steps (#45).
- Centralized observation-based reset fallback timing across the dashboard, widgets, Now Bar, and Wear surfaces (#47).

### Fixed

- App update and release-history loading now use a centered One UI spinner instead of clipped status text, and idle update cards no longer reserve empty status-line height (#35).
- Live usage monitor progress uses a plain progress dot, and Now Bar / notification Codex logos adapt to light, dark, and accent surfaces without a baked white square (#36).

### Development

- Restructured the repository into `android/` and `ios/` project roots with thin root wrappers and CI paths pointed at the Android Gradle tree (#40).
- Fixed Cloud Agent setup to invoke `android/gradlew` after the monorepo move (#43).
- Expanded regression coverage for settings transfer, update-check frequency, usage-pace estimates, widget fill controls, Material You preferences, and Wear sync contracts (#37, #38, #42, #44, #45, #47).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.3.3...v2.4.0 <!-- pragma: allowlist secret -->

## 2.3.3 — 2026-07-17

### Added

- Now Bar percentage-mode setting (Auto / 5-hour / Weekly) so users choose which remaining percentage drives the live-monitor progress pill, with Auto locking to the auto-start trigger window (#33).
- A `W` prefix on weekly-focused Live Update critical percentage text so compact Now Bar labels stay unambiguous (#33).

### Changed

- Normalized app, widget, and OAuth action buttons to a full-pill corner radius with consistent 18sp bold labels (#32).
- Changing the percentage mode safely re-posts an active monitor, stops the session if posting fails, and restores Auto to the original auto-start lock instead of recomputing lower-remaining (#33).

### Development

- Expanded regression coverage for Now Bar percent-mode selection, settings-change focus resolution, and failed applyPercentModeChange cleanup (#33).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.3.2...v2.3.3 <!-- pragma: allowlist secret -->

## 2.3.2 — 2026-07-15

### Added

- Automatic, Android Live Update, and Samsung compatibility display modes for the live usage monitor, including manual overrides for firmware that reports promotion support incorrectly (#29).
- In-app diagnostics for blocked app or live-monitor notifications, plus Samsung developer-option and firmware troubleshooting guidance (#29).

### Changed

- Automatic mode now keeps Android 16 Live Update promotion and Samsung's private ongoing-activity payload mutually exclusive, selecting the Samsung-compatible path when Galaxy firmware denies platform promotion (#29).
- Changing display modes safely re-posts an active monitor, while failed updates clear stale active state instead of leaving the monitor stuck (#29).

### Fixed

- Restored Now Bar presentation on compatible Samsung firmware where third-party Android Live Updates remain ordinary notifications, while retaining the standard notification fallback on unsupported firmware (#29).

### Development

- Added regression coverage for display-mode normalization and firmware-dependent mode resolution (#29).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.3.1...v2.3.2 <!-- pragma: allowlist secret -->

## 2.3.1 — 2026-07-14

### Added

- Configurable Now Bar auto-start when remaining allowance reaches a selected threshold for five-hour, weekly, or both windows, with dismiss suppression for the remainder of that monitor window (#26).
- Rendered GitHub release-note Markdown (headings, lists, emphasis, and links) in the App update What's new section and on each Release history card (#25).

### Changed

- Treats every release before 2.3.0 as irreversible for in-app installs and sends users to the GitHub release page instead, because those builds lacked a working updater against the canonical repository (#25).

### Development

- Expanded regression coverage for Now Bar auto-start thresholds and release-note Markdown / irreversible-update policy (#25, #26).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.3.0...v2.3.1 <!-- pragma: allowlist secret -->

## 2.3.0 — 2026-07-14

### Fixed

- Rebuilt the Android 16 live usage notification on a dedicated default-importance channel, removed conflicting legacy Samsung metadata, and exposed actual Live Update promotion state for compatible Samsung Now Bar surfaces (#21).
- Improved reset-duration contrast on dashboard usage cards and hid misleading reset countdowns while a usage window remains completely unused, including home and lock-screen widgets (#23).
- Restored in-app update discovery and project links by pointing them at the canonical repository instead of a stale fork (#24).

### Development

- Centralized production GitHub release URLs and added regression guards that prevent the stale update source from being reintroduced (#24).

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.2.0...v2.3.0 <!-- pragma: allowlist secret -->

## 2.2.0 — 2026-07-14

### Added

- Configurable reset-credit expiry reminders with multiple custom lead times, per-credit scheduling, reboot restoration, and automatic cancellation after sign-out or redemption (#15).
- A reminder action that opens the existing in-app confirmation before redeeming an expiring reset credit (#15).
- Secure in-app update discovery with daily checks, a dashboard update card, manual checks, and release history (#16).
- Verified APK installation that checks HTTPS trust, file size, SHA-256 integrity, package identity, version code, and signing certificate before handing an update to Android (#16).
- Upgrade recovery that preserves widget providers and options, then repairs existing widgets asynchronously after package replacement (#16).
- An optional live usage monitor showing real five-hour and weekly allowance values in Android 16 Live Updates and compatible Samsung Now Bar surfaces, with a standard notification fallback on earlier Android versions (#18).

### Changed

- The live usage monitor refreshes from new usage snapshots, restores after reboot or app replacement, marks unavailable windows clearly, and stops at the next valid reset or when dismissed, stopped, or signed out (#18).
- Reset-credit expiry reminders revalidate credit state immediately before notifying and coalesce duplicate scheduling state (#15).

### Fixed

- Dashboard usage-card labels now use theme-aware foreground colors and remain readable in dark mode (#19).
- Release-history toolbar and Android back navigation now return through the update flow correctly (#16).
- Expired five-hour usage windows no longer prevent the live monitor from falling back to a future weekly reset (#18).

### Development

- Added a debug-only local release fixture for end-to-end updater testing without weakening production update trust (#16).
- Expanded regression coverage for expiry-reminder planning, release parsing and integrity checks, widget upgrade repair, live-monitor lifecycle, and dark-mode usage labels (#15, #16, #18, #19).

### New Contributors

- @Robertg761 made their first contribution in https://github.com/BenItBuhner/Codex-Meter/pull/18. <!-- pragma: allowlist secret -->

**Full Changelog**: https://github.com/BenItBuhner/Codex-Meter/compare/v2.1.0...v2.2.0 <!-- pragma: allowlist secret -->

## 2.1.0 — 2026-07-13

### Added

- A resumable four-step One UI onboarding flow for first launch, including sign-in, skip, upgrade, and predictive-back handling (#13).
- A responsive light/dark OAuth completion page that returns users to the app after browser sign-in (#13).
- Optional notifications when five-hour or weekly allowances unexpectedly refill before their scheduled reset (#12).
- An independent notification preference for increases in available reset credits (#12).

### Changed

- Settings now uses an expanded, collapsible One UI toolbar that keeps primary controls within one-hand reach (#9).
- Local-data and privacy details now live in a dedicated Settings section instead of the dashboard (#10).
- Reset-credit observations are coalesced across usage and detail responses to prevent duplicate increase notifications (#12).

### Fixed

- Suppressed false refill celebrations after reset-credit redemption, including delayed backend propagation (#12).
- Retried credit notifications when Android previously blocked application- or channel-level delivery (#12).
- Cleaned up abandoned OAuth services when onboarding is skipped and hardened callback handling across Android versions (#13).

### Development

- Removed obsolete release binaries, recovery documents, generated caches, and superseded direct-build tools (#11).
- Documented the Android toolchain, package-authentication requirements, and release validation path for cloud builds (#8).

## 2.0.0 — 2026-07-12

### Added

- Samsung One UI-native dashboard, settings, widget configuration, and About surfaces.
- Responsive ring, four-dial, and battery-list home-screen widget layouts.
- Dedicated five-hour and weekly Samsung lock-screen widget providers.
- Updated adaptive icons, theme assets, previews, opacity controls, and usage wave presentation.
- Gradle wrapper and reproducible Android build configuration with vendored OneUI-Design metadata.

### Changed

- Reworked navigation, account controls, refresh preferences, and notification settings around SESL components.
- Updated widget rendering and sizing to follow the dimensions supplied by Android and One UI hosts.
- Simplified the release build script to use the Gradle Android plugin.

### Fixed

- Prevented the One UI pull-to-refresh indicator from crashing when its four-dot drawable starts.
- Added deduplicated low-usage alerts for both usage windows and notifications when reset-credit inventory increases.
- Decoupled reset-time reminders from the low-usage threshold and added an immediate notification test action.

## 1.7.0 — 2026-07-11

### Added

- Native lock-screen countdowns for the five-hour and weekly reset timestamps.
- Local countdown updates that do not require additional server requests.
- Reset-alert preferences for silent, notification-sound, and alarm-sound delivery.
- Per-limit alert selection and remaining-percentage thresholds.
- Alarm restoration after reboot and application replacement.

### Changed

- Revised Samsung-style lock-screen gauge geometry with a heavier stroke, smaller diameter, lower visual center, and more deliberate bottom gap.
- Reset alerts now trigger a background usage refresh and update all widget surfaces after the scheduled reset time.

### Security and behavior

- Notification permission is requested only when alerts are enabled on Android 13 or later.
- Exact-alarm capability is checked on supported Android versions; the scheduler falls back to an inexact idle-aware alarm when unavailable.
- Signing out cancels pending reset alerts.

## 1.6.0 — 2026-07-11

### Added

- Detailed Codex reset-credit inventory, expiration display, confirmation, and redemption.
- Automatic usage and reset-credit refresh after a successful redemption.
- Both-window, five-hour-only, and weekly-only widget configurations.
- Optional reset-credit count, expiration, and reset action on widgets.

### Changed

- Supersampled lock-screen ring and gauge geometry.
- Native Android text overlays and native bar progress elements for sharper Samsung lock-screen rendering.
- Single-measurement layouts now reclaim unused space.

## 1.5.0 — 2026-07-11

### Added

- Eight Samsung lock-screen/AOD picker entries: Numbers, Rings, Gauges, and Bars, each in Square (1×1) and Wide (2×1) sizes.
- Dedicated monochrome preview artwork and runtime renderers for every graphic lock-screen style.
- Size-aware lock-screen bitmap rendering using Samsung/Android host-provided `OPTION_APPWIDGET_SIZES` when available.

### Changed

- Preserved the existing square and wide numeric provider component names so installed 1.4.0 tiles remain valid after upgrading.
- Exposed graphic choices as separate provider components rather than relying on a lock-screen host configuration activity that One UI does not consistently surface.
- Extended the optional Samsung ServiceBox compatibility response to all eight style/size combinations.

### Verified

- Compiled and decoded all eight provider and companion metadata resources.
- Restricted lock-screen layouts to RemoteViews-safe `FrameLayout`, `TextView`, and `ImageView` classes.
- Signed with the same certificate as 1.2.0–1.4.0 for direct installation over the previous release.

## 1.4.0

- Replaced the legacy complication-marker provider with current One UI monotone AppWidget metadata and category routing.
- Added distinct 1x1 square and 2x1 wide lock-screen/AOD providers.
- Exported the compact providers so Samsung SystemUI can deliver update broadcasts.
- Reduced lock-screen layouts to RemoteViews-safe FrameLayout/TextView trees.
- Added Samsung companion metadata, the private 0x2000 widget category, and an optional ServiceBox RemoteViews response path.


## 1.3.0 — 2026-07-11

### Added

- Toggleable widget title, disabled by default for new and existing widget configurations.
- Samsung One UI compact lock-screen complication provider with standard keyguard fallback.
- Samsung One UI Home native frame/blur metadata, supported cell-size metadata, and size-specific picker previews.
- Samsung integration status and lock-screen setup guidance in Settings.

### Changed

- Reworked One UI 8+ app mode with a larger viewing-area header, Samsung-blue section labels, flat grouped cards, Galaxy-style spacing, and compact toolbar controls.
- Removed app-drawn corner rounding from Samsung One UI widget surfaces so the launcher can own the frame.
- Switched widget text to Samsung's `sec` family when available, with normal platform fallback elsewhere.
- Version 1.3.0 reuses the 1.2.0 signing certificate and supports in-place installation.

### Fixed

- Removed the default `CODEX` text from picker previews and initial widget layouts.
- Made successful snapshot persistence and refresh-error clearing atomic.
- Suppressed misleading red refresh warnings while a newly fetched snapshot is still current.
- Removed clipped toolbar buttons and inconsistent control minimum heights.

## 1.2.0 — 2026-07-10

### Added

- Automatic, Large, and Maximum ring/gauge sizing policies.
- Fully transparent widget backgrounds.
- Five- and ten-minute best-effort refresh choices using chained one-shot jobs.
- Separate Material expressive and One UI visual-language choices for the app and each widget.
- Large and maximum ring/dial layouts with orientation-aware launcher sizing.
- New progress-terminal adaptive icon and monochrome themed icon.

### Changed

- Reworked app cards, buttons, dropdowns, spacing, and corner radii into consistent shadow-free surface systems.
- Normalized widget bitmap density so graphics fill high-density screens correctly.
- Ring and dial resize updates now render one bitmap tier at a time to avoid Binder transaction overflows.
- Increased the widget provider's supported maximum dimensions.

### Fixed

- Undersized circular graphics on high-density devices.
- Graphic-tier selection that previously underestimated the widget's current portrait or landscape dimensions.
- Inconsistent overlapping shadows and radii in the application UI.

## 1.1.0 — 2026-07-10

### Fixed

- Added the missing `ACCESS_NETWORK_STATE` permission required by connectivity-constrained `JobScheduler` jobs.
- Prevented background scheduling errors from crashing OAuth completion, later launches, boot handling, or widget refreshes.
- Returned a successful browser callback immediately after encrypted credentials are committed.
- Preserved authenticated state when the initial usage request or post-login scheduling fails.
- Added a deep link and explicit browser button for returning to the application.
- Cleared stale OAuth state and hardened repeated-launch behavior.
- Corrected bar progress direction when displaying percentage used.

### Added

- Adaptive, bars, rings, dials, and minimal widget styles.
- Responsive Android 12+ `RemoteViews` layouts for multiple launcher sizes.
- Compact and comfortable density choices.
- Four additional accents and a monochrome option.
- Additional background transparency, reset-label, plan-label, updated-time, and refresh-button controls.
- In-application live widget previews.
- Original progress-knot adaptive and monochrome application icon.

## 1.0.0 — 2026-07-10

- Initial native Android application, OAuth login, Codex usage retrieval, and home-screen widget release.
