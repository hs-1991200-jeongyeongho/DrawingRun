<p align="center">
  <img src="https://github.com/user-attachments/assets/3dd3c7e2-fc8b-4e65-b3ac-c8af00b0ce26" width="200" alt="DrawingRun 로고"/>
</p>

<div align="center">

# 🏃 DrawingRun

사용자가 원하는 모양의 러닝 경로를 제공하는 GPS 기반 애플리케이션

</div>

---
## 📚 목차

- [개요](#-개요)
- [팀원 소개](#-팀원-소개)
- [주요 기능](#-주요-기능)
- [프로젝트 구조 및 사용 기술](#-프로젝트-구조-및-사용-기술)
- [기대효과](#-기대효과)
- [앱 주요 화면](#-구현-화면)
- [시연 영상](#-시연-영상)
- [확장 가능성과 아쉬운 점](#-확장-가능성과-아쉬운-점)
- [마무리](#-마무리)


---

## 📖 개요

**DrawingRun**은 사용자가 지도 위에 직접 그림을 그리거나,  
미리 준비된 경로를 선택해 러닝을 즐기고 기록할 수 있는 앱입니다.

GPS 아트는 실시간 위치 기록을 통해 지도 위에 그림을 그리는 활동으로,  
운동을 개성 있게 즐기고 싶은 러너들 사이에서 주목받고 있습니다.

하지만 경로 설계의 복잡함은 초보자에게 진입 장벽이 될 수 있습니다.  
**DrawingRun**은 이 장벽을 낮추기 위해 저장된 경로를 제공하거나,  
직접 그려진 경로로 바로 러닝을 시작할 수 있도록 도와줍니다.

---

## 👥 팀원 소개

| 학번     | 이름   | 역할                                |
|----------|--------|-------------------------------------|
| 2071066  | 임동현 | 팀장 / 경로 생성 및 좌표값 추출 / 그림 인식 모델 시도         |
| 1971396  | 김인서 | UX 설계 / 기능 흐름 구성              |
| 1991200  | 전경호 | UI 디자인 / 시각 스타일 설계          |
| 2171042  | 손지형 | DB 관리 / 드로잉 기능 구현             |
| 2171087  | 박수빈 | 경로 리스트 구성 / 그림 선택 유도 UI 설계 / 그림 인식 모델 시도 및 전처리 |


---

## ✨ 주요 기능

- 🎨 지도 위에 직접 그림을 그려 나만의 러닝 경로 생성
- 📂 미리 저장된 경로(그림 라벨) 선택으로 간편한 시작
- 🏃‍♀️ GPS 기반 실시간 위치 추적 및 러닝 기록
- 📸 러닝 종료 후 경로 캡처 이미지 저장
- 📊 거리, 시간, 속도, 칼로리 등 주요 운동 정보 표시

---

## 🏗 프로젝트 구조 및 사용 기술

<p align="center">
  <img src="https://github.com/user-attachments/assets/1c84f46a-b2f6-47d5-95fd-2ece9626fa54" alt="프로젝트 구조도" width="700"/>
</p>

### 개발 환경
![Windows](https://img.shields.io/badge/Windows_10%2F11-0078D6?style=for-the-badge&logo=windows&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)

### 개발 언어
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)

### 개발 도구 & API
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Google Maps API](https://img.shields.io/badge/Google%20Maps%20API-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)

---

## 🌱 기대효과

- **자율성**: 사용자가 원하는 경로를 직접 선택하거나 그리는 방식으로 운동 루틴 구성 가능
- **창의성**: GPS 아트를 통해 창의적인 표현과 러닝이 결합된 즐거운 운동 경험 제공
- **확장성**: 이미지로 저장된 경로를 기록∙공유하거나, 웰니스∙관광 등 다양한 분야로 확장 가능

---

## 📱 구현 화면

#### Android 앱 화면

<details>
  <summary>▶️ Click! 이미지를 누르면 자세히 볼 수 있습니다.</summary>

  <br>

  <img src="https://github.com/user-attachments/assets/45b22a0a-0ba4-46e4-80f2-23c3ef7e0d0a" alt="Android 앱 화면" width="700"/>
</details>

---

## 🎬 시연 영상

[![YouTube 버튼](https://github.com/user-attachments/assets/2d75ff29-e853-4575-84c2-06a5543185d2)](https://youtu.be/YO_AIYwtn2s)

---

## 🔄 확장 가능성과 아쉬운 점

초기 기획 단계에서는 사용자가 지도 위에 자유롭게 그림을 그리면,  
이를 인공지능 모델이 인식하여 **그림이 무엇인지 판단**하고,  
그에 해당하는 라벨(예: 고양이, 별, 꽃 등)을 매칭하여  
**데이터베이스에 미리 저장된 해당 라벨의 경로를 추천**하는 흐름을 구상했습니다.

실제로 그림 인식 모델을 개발하고 실험도 진행했지만,  
다음과 같은 이유로 최종 구현에서는 제외하게 되었습니다:

- **학습된 라벨 종류가 한정적**인 상황에서,  
  사용자가 자유롭게 그린 그림이 라벨 목록에 없다면  
  인식이 왜곡되거나 전혀 엉뚱한 결과가 출력될 수 있음
- 이는 곧 사용자에게 혼란을 주고, 앱 사용 경험을 저하시킬 수 있음
- 또한, 이미 데이터베이스에 라벨링된 경로들이 존재하는 상황에서  
  굳이 그림을 그리고 다시 모델을 거치는 구조는 **UX 흐름상 비효율적**

이에 따라, 
사용자가 그림을 그리는 대신 **미리 저장된 경로 라벨을 직접 보고 선택하는 구조로 전환**하였습니다.

---

하지만 장기적으로는,
- 사용자가 그린 그림을 지도상 **도보 가능 경로 위로 자동 매핑**하거나,
- 실제 도로 구조를 반영해 **새로운 경로를 생성해주는 로직**이 구현된다면,  
  더욱 창의적이고 몰입감 높은 러닝 경험을 제공할 수 있을 것입니다.

따라서 현재의 선택은 현실적인 UX 최적화를 위한 결정이지만,  
**향후 기술 발전에 따라 다시 도입 가능한 확장 아이디어로 남아 있습니다.**


---

## 📌 마무리

**DrawingRun**은 운동의 지속성과 창의성을 결합한 새로운 러닝 방식입니다.  
누구나 쉽게 접근하고, 나만의 개성을 표현하며 운동을 즐길 수 있는 이 앱은  
앞으로 웰니스, 도시 관광, SNS 콘텐츠로도 확장 가능한 잠재력을 가지고 있습니다.

감사합니다!

---

<details>
  <summary><strong>🎨 아이콘 출처 보기</strong></summary>

  <br>

  아래는 본 프로젝트에서 사용된 아이콘의 출처입니다.  
  모든 아이콘은 [Flaticon](https://www.flaticon.com/kr/)에서 제공되었습니다.

  <a href="https://www.flaticon.com/kr/free-icons/" title="음표 아이콘">음표 아이콘 제작자: Pixel perfect - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="눈송이 아이콘">눈송이 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/-" title="관광 여행 아이콘">관광 여행 아이콘 제작자: Solid Icon Co - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="사이클링 아이콘">사이클링 아이콘 제작자: sonnycandra - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="구근 아이콘">구근 아이콘 제작자: Anggara - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="자동차 아이콘">자동차 아이콘 제작자: sonnycandra - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="디즈니 아이콘">디즈니 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="고양이 아이콘">고양이 아이콘 제작자: Icon Mela - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="데스크탑 아이콘">데스크탑 아이콘 제작자: kerismaker - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="왕관 아이콘">왕관 아이콘 제작자: Dewi Sari - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/-" title="개 코 아이콘">개 코 아이콘 제작자: POD Gladiator - Flaticon</a><br>
   <a href="https://www.flaticon.com/kr/free-icons/" title="머리 아이콘">머리 아이콘 제작자: pojok d - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="물고기 아이콘">물고기 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="꽃 아이콘">꽃 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="기타 아이콘">기타 아이콘 제작자: InfoBrother - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="심장 아이콘">심장 아이콘 제작자: kendis lasman - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="말 아이콘">말 아이콘 제작자: Vector Stall - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="연필 아이콘">연필 아이콘 제작자: Pixel perfect - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="토끼 아이콘">토끼 아이콘 제작자: kerismaker - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="무지개 아이콘">무지개 아이콘 제작자: amonrat rungreangfangsai - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="로켓 아이콘">로켓 아이콘 제작자: Icon Mela - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="배 아이콘">배 아이콘 제작자: Creatype - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="두개골 아이콘">두개골 아이콘 제작자: juicy_fish - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="별 아이콘">별 아이콘 제작자: Pixel perfect - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="태양 아이콘">태양 아이콘 제작자: Good Ware - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="나무 아이콘">나무 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="자전거 아이콘">자전거 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="별자리 아이콘">별자리 아이콘 제작자: Pixel perfect - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/-" title="나비 넥타이 아이콘">나비 넥타이 아이콘 제작자: meaicon - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="동물학 아이콘">동물학 아이콘 제작자: Nur syifa fauziah - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/-" title="원 모양 아이콘">원 모양 아이콘 제작자: Smashicons - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="구름 아이콘">구름 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="결석 아이콘">결석 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="포크 아이콘">포크 아이콘 제작자: Roundicons - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="손 아이콘">손 아이콘 제작자: visuallanguage - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/-" title="홈 버튼 아이콘">홈 버튼 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="비밀 아이콘">비밀 아이콘 제작자: Mihimihi - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="달 아이콘">달 아이콘 제작자: Uniconlabs - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/monserrate-hill" title="monserrate hill 아이콘">Monserrate hill 아이콘 제작자: Deylotus Creative Design - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="네일 아이콘">네일 아이콘 제작자: riajulislam - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="삽 아이콘">삽 아이콘 제작자: muh zakaria - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="숟가락 아이콘">숟가락 아이콘 제작자: Roundicons - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/-" title="둥근 직사각형 아이콘">둥근 직사각형 아이콘 제작자: Freepik - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="삼각형 아이콘">삼각형 아이콘 제작자: See Icons - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/-" title="병 장식 아이콘">병 장식 아이콘 제작자: Andreas666 - Flaticon</a><br>
  <a href="https://www.flaticon.com/kr/free-icons/" title="피자 아이콘">피자 아이콘 제작자: Freepik - Flaticon</a><br>
  

</details>
