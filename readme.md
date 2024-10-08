# Marathon-Assignment

- 취뽀컴트루 & 취준마라톤 과제 레포
- 각 프로젝트 설명은 assignment 폴더를 참고 바랍니다.

| 과제 이름 | 한 줄 설명 | 예상 난이도 (1 - 5) | 링크 |
| -- | -- | -- | -- |
| My-Storage | 간단한 형태의 클라우드 스토리지를 만들어 봅시다. | 4 ~ 5 | [링크](assignments/my-storage.md) |
| Mini-Pay | 기본 송금과 정산 기능이 있는 페이 서비스를 만들어 봅시다. | 4 | [링크](assignments/mini-pay.md) |
| Accelerated To-Do | iCal 형식에 맞춘 일정을 제공하는 To-Do 서버를 개발해 봅시다. | 4 ~ 5 | [링크](assignments/accelerated-to-do.md) |
| Opener-Market | 구매자와 판매자 기능이 있는 오픈마켓 서비스를 개발해 봅시다. | 3 ~ 4 | [링크](assignments/opener-market.md) |
| Popping-Community | 일반적인 커뮤니티를 개발해 봅시다. 단, 사용자가 엄청나게 많을 겁니다. | 3 ~ 4 | [링크](assignments/popping-community.md) |
| Algorithm-Market | 알고리즘 연습 사이트 (ex. 릿코드, 백준, 프로그래머스) 를 개발해 봅시다. | 4 | [링크](assignments/algorithm-market.md) |
| Very-Simple-SNS | 간단한 형태의 SNS를 개발해 봅시다.<br />어느정도 규모 있는 서버 개발을 해 본적이 없는 분들에게 추천드립니다. | 2 | [링크](assignments/very-simple-sns.md) |

## Branch Guide

- c4-cometrue org에 존재하는 레포에 본인의 대표 브랜치를 생성합니다.
- 이후, 각 Step 마다 해당 브랜치를 베이스로 하는 새로운 브랜치를 생성합니다.
    - 이 경우, 각 브랜치의 이름은 `feature/{대표 브랜치 이름}_step0` 과 같은 방식으로 생성합니다.
    - PR은 대표 브랜치 <- Step 브랜치로 진행합니다.
- 필수적으로 PR Approve를 받아야 합니다. PR을 올리신 이후엔 리뷰어 지정 부탁드립니다. (@VSFe)
    - 가능하다면, 참여하시는 모든 멤버를 리뷰어로 지정해주세요. 서로의 코드를 읽고, 코멘트 하는 것도 많은 도움이 됩니다.

## Git Convention

### 포맷

```
type: subject

body
```

#### type

- 하나의 커밋에 여러 타입이 존재하는 경우 상위 우선순위의 타입을 사용한다.
- fix: 버스 픽스
- feat: 새로운 기능 추가
- refactor: 리팩토링 (버그픽스나 기능추가없는 코드변화)
- docs: 문서만 변경
- style: 코드의 의미가 변경 안 되는 경우 (띄어쓰기, 포맷팅, 줄바꿈 등)
- test: 테스트코드 추가/수정
- chore: 빌드 테스트 업데이트, 패키지 매니저를 설정하는 경우 (프로덕션 코드 변경 X)

#### subject

- 제목은 50글자를 넘지 않도록 한다.
- 개조식 구문 사용
    - 중요하고 핵심적인 요소만 간추려서 (항목별로 나열하듯이) 표현
- 마지막에 특수문자를 넣지 않는다. (마침표, 느낌표, 물음표 등)

#### body (optional)

- 각 라인별로 balled list로 표시한다.
    - 예시) - AA
- 가능하면 한줄당 72자를 넘지 않도록 한다.
- 본문의 양에 구애받지 않고 최대한 상세히 작성
- “어떻게” 보다는 “무엇을" “왜” 변경했는지 설명한다.

## Additional Requirement

각 PR의 요구사항과 더불어, 해당 명세를 **반드시** 만족해야 합니다.

- 매 Step 마다 테스트 코드를 작성해보세요.
  - 커버리지 80% 이상을 맞추지 못할 경우, PR이 제한됩니다.
- Code Smell을 최소화 하세요.
  - SonarQube를 사용합니다. PR 과정에서 SonarQube Major Issue 발견 시, PR이 제한됩니다. 
- Code Convention 을 사용하여 코드를 작성해 주세요.
- 여기서는 **네이버 핵 데이 컨벤션**을 사용합니다.
    - 출처: https://naver.github.io/hackday-conventions-java/
- 가능하면, 다른 분들의 PR도 코멘트를 달아보도록 노력해보세요. 상대방의 코드를 지적하는 것만이 코드 리뷰가 아닙니다. 코드를 보고 배울 점이 있다고 생각해도, 가감없이 코멘트를 달아주세요.