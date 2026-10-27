# GPT HUD 정식3.1.6 / code93 — 2026-10-27

## 배포 후 홈 초기화 시각 표시 복원 — 로컬 수정 / 미배포

- 사용자 피드백: 업데이트 후 홈에 남은 시간만 보이고 정확한 초기화 날짜·시각이 사라졌다. 개편 때 `BetaQuotaUi`의 compact 분기가 절대 시각을 상세로 옮긴 것이 원인이다.
- 홈의5시간·주간 한도 모두 기존 `home_quota_reset_schedule` 두 줄 서식을 사용하도록 복원했다. 기기 시간대·12/24시간 설정과 기존 카운트다운을 유지한다. 자료 없음/시각 미제공/만료 상태를 임의의 미래 시각으로 바꾸지 않는다.
- 생산 수정은 `BetaQuotaUi.java`의 표시 분기 한 곳이다. 기준 공개 HEAD `59c6f2e0cf266165c3d84b48b11b2cabaff0ee20`. 버전·공개APK·태그·릴리스는 변경하지 않았다.
- `verify-stable316-source.py`, `verify-stable316.py`, Release 빌드/lint와 `git diff --check` 통과. lint 추가 오류0/기존 경고281, 기존 baseline 유지.
- 전용 합성 `gpthud_stable316_preview`에서 `HomeQuotaScheduleNativeTest`를 기본360dp/글씨1배와320dp/글씨2배로 실행해 각각57검사 통과. 실제 홈 두 초기화 시각·줄바꿈·텍스트 높이, 미래/만료/미제공/빈 자료/상대 reset 시각과 암호화 합성 로그인 불변을 확인했다. 임시 합성 사용량 캐시는 검사 후 원복했다. 실제폰·실계정·새 공개 자동 업데이트는 이번 수정에서 검증하지 않았다.
- 증거: `android/build/stable316/home-reset-*.log`, `native/screens/stable316/home-reset-restored-*.png`. 공개316 배포 증거와 로컬 수정 증거를 구분한다.

사용자가 검토한 정식 개편을3.1.6으로 올려 기존 자동 업데이트 경로에 공개하도록 직접 승인했다. 기준 HEAD `9a7da3cdd39692cd39fc153385a5aa85a0a78b25`, 작업 브랜치 `release/3.1.6`.

## 구현 범위

- 홈: 티어·이름·칭호·레벨·남은5시간/주간 한도 통합 → 이번 주간 사용량 → AI의 한마디. 중복 안전 영역과 카드 껍질·간격을 정리한다.
- 기록·성장·도감: 월 경계 날짜 누락, 관측 상태, 보유/진행 요약과 읽기 오류/재시도, 작은 화면과 큰 글씨를 개선한다.
- 설정: 전체 사용량 DB의 진입 시 조회를 제거하고 고급 항목을 지연 생성한다. 자동 업데이트 설정과 접근성 선택/펼침 상태를 제공한다.
- 계정·백업: 삭제/내보내기를 시작한 계정에 작업을 고정한다. 백업 진행/중복 방지·계정/선택/파일 유지·화면 재생성·설정 전용 복원 안내를 보완한다.
- 기존 정식 `dev.bennett.codexmeter`, 서명·OAuth·DB/설정 형식·성장식/XP원본·업적/칭호/장착/메달/보관함·624한마디ID/40팁을 보존한다. 별도 베타 빌드/배포 없음. Wear3.1.0/code87, Windows3.1.1 유지.
- 구현 파일별 이유와 개편 자체의 상세 검증은 [개편 정본](STABLE_REDESIGN.ko.md)에 기록했다. 그 문서의3.1.5/code92 로컬 후보 설명은 이전 단계다.

## 검증

- `python android/tests/verify-stable316-source.py`: 기준 대비 허용 변경과 보호 소스/manifest/카탈로그/신뢰 주소/이전 테스트·Wear·Windows 및 version93/history를 확인한다. 기존315·redesign 파일을 새 버전에 맞춰 덮어쓰지 않고316 진입점을 추가했다.
- `python android/tests/verify-stable316.py`: 기존 core72,762 + 날짜9,137 + 계정13 + 연결8 +316업데이트21검사 통과. `verify-stable314-release.py`의 foreground147/cache·storage10/parser19도 통과.
- Release 빌드·lint 성공. 추가 오류0, 기존 경고281, baseline으로 제외되는53오류/111경고 유지. baseline을 갱신하지 않았다.
- 기존 인증서 SHA256 `6abd39b13e561cd67fa0d76ab79f9f22500cf5378dc60b176bfe18bf0d2f8be3`, v2서명/패키지/3.1.6/code93 통과. 공식폰트·54archive/1APK 검사 통과.
- 공개 전 익명 업데이트 목록352391bytes로 과거524288bytes 한도/32768bytes 여유 통과. 게시 후 실제 목록과 공개 APK를 다시 확인한다.
- 전용 합성 AVD `gpthud_stable316_preview`와 `gpthud_stable316`만 사용한다. 공개315→로컬316 보존과 공개315→앱 다운로드→Android Update→공개316 설치 증거를 분리한다. 실제 실행 결과는 아래 실행 결과와 릴리스 본문/로컬 전달 결과가 기준이다.

## 업데이트 방법과 보존 경계

자동 확인이 켜져 있으면 앱 시작·복귀 때 기존5분 캐시와 버전당 한 번 안내를 사용한다. 즉시 푸시·무인 설치가 아니며 Android 설치 승인이 필요하다. 바로 확인하려면 기록/홈/성장 → 설정 → 앱 정보 → 업데이트 확인을 사용한다. 앱 삭제·데이터 초기화·재로그인을 요구하지 않는다.

기존315까지의 릴리스·다운로드/서명과 SOURCE 미커밋 변경을 보존한다. 새 키·권한·런타임 AI API·서버·클립보드 감시·Drive 업로드를 추가하지 않는다.

실물폰/실계정 OAuth 성공·실사용 데이터 복원·설치 출처 허용 설정 복귀·TalkBack 낭독·OEM/장기 백그라운드·위젯/Wear/Windows 물리 연동은 미검증이다. 합성 암호화 로그인 보존은 실제 OAuth 서버 성공을 뜻하지 않는다.

## 실행 결과

- 공개 전 preview AVD의 공개315→로컬316 교체: seed9/교체 전43/교체 후49/재실행49검사 통과. 실제 OAuth 서버 성공이 아닌 합성 암호화 인증·기록 보존 검증이다.
- 첫 화면 계측의 메달 fixture 미준비와 첫 앱 시작의 Locale 초기화 준비 실패를 기록했다. 합성 메달 입력과 AppCompat Locale 사전 초기화를 보완했으며 실제 언어 검사를 유지한다.

공개 게이트와 실제 설치 결과는 공개 릴리스 [v3.1.6](https://github.com/ldjkorea/DJS-GPT-HUD/releases/tag/v3.1.6), 로컬 `SOURCE/delivery/stable-3.1.6/release-20261027/final-verified/VERIFICATION.ko.md`, 작업점 `android/build/stable316`의 실행 로그/영수증에서 확인한다. 공개 전 준비와 실제 게시 완료를 구분한다.
