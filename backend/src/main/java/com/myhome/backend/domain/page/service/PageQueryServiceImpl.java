package com.myhome.backend.domain.page.service;

import com.myhome.backend.domain.monitoring.dto.ServiceResponse;
import com.myhome.backend.domain.monitoring.service.MonitoringQueryService;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.AboutProfileResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.AboutResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ContactLinkResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ContactResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ContentItemResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.CtaLinkResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ExperienceResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HighlightResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HomeHeroResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HomeOperationsResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HomeOverviewResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HomeResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.HomeServerSnapshotResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.LabFeatureResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.NavigationCardResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.OverviewContentResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ProjectItemResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ProjectLinkResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ProjectPageResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.ServicePageResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.SkillCategoryResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.SkillItemResponse;
import com.myhome.backend.domain.page.dto.FrontendPageResponses.StrengthResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageQueryServiceImpl implements PageQueryService {

	private static final String EMAIL = "zerojae175@gmail.com";
	private static final String GITHUB_URL = "https://github.com/YeongJae0114";
	private static final String BLOG_URL = "https://velog.io/@yjl8628/posts";
	private static final String LINKEDIN_URL = "https://www.linkedin.com/in/zerojae";

	private final MonitoringQueryService monitoringQueryService;

	@Override
	public HomeResponse getHome() {
		List<HomeServerSnapshotResponse> servers = monitoringQueryService.getMonitoringServers().servers().stream()
				.limit(3)
				.map(server -> new HomeServerSnapshotResponse(
						server.id(),
						server.name(),
						"Home Lab",
						server.role(),
						server.status(),
						server.latencyMs()
				))
				.toList();
		String operationStatus = servers.stream().anyMatch(server -> "offline".equals(server.status()))
				? "offline"
				: "online";

		return new HomeResponse(
				new HomeHeroResponse(
						"Java/Spring Backend Engineer",
						"운영 가능한 백엔드와 신뢰할 수 있는 시스템을 설계합니다.",
						"Java, Spring, DB, 인프라 운영 경험을 기반으로 서비스가 배포된 뒤에도 오래 버티는 구조를 만드는 개발자 브랜드 페이지입니다.",
						ctaLinks(),
						List.of(
								new HighlightResponse("Core", "Spring"),
								new HighlightResponse("Data", "SQL"),
								new HighlightResponse("Ops", "Status")
						),
						new HomeOperationsResponse(
								"platform snapshot",
								"Live Operations Board",
								operationStatus,
								servers,
								"next extension point",
								"Status API, Notion sync, GitHub activity, Local LLM query panel can attach here without changing the page composition."
						)
				),
				new HomeOverviewResponse(
						"Quick Overview",
						"최근 작업과 대표 신호를 한눈에",
						"홈은 전체 메뉴를 나열하기보다 대표 프로젝트, 최신 기록, 진행 중인 실험을 먼저 보여주는 기술 플랫폼 대시보드로 구성했습니다.",
						new OverviewContentResponse("home-lab", "Home Lab Monitoring", "개인 서버와 API 서비스를 관측하는 홈랩 모니터링 구성입니다.", "/project", List.of("Nginx", "Prometheus", "Docker")),
						new OverviewContentResponse("spring-auth-audit", "Spring 인증 흐름에 감사 로그를 붙이는 기준", "로그인, 토큰 갱신, 권한 실패 이벤트를 운영 관점에서 추적하는 방법을 정리합니다.", "/blog", List.of("Spring Security", "Audit", "Operations")),
						new OverviewContentResponse("infra-monitor", "Infra Monitoring API", "서버 상태 데이터를 실시간 API로 연결하고 알림 정책을 붙이는 기능", "/service", List.of("status", "prometheus", "alert")),
						navigationCards()
				)
		);
	}

	@Override
	public AboutResponse getAbout() {
		return new AboutResponse(
				new AboutProfileResponse(
						"저는 Java와 Spring 기반의 백엔드 시스템을 설계하고 운영하는 개발자입니다. 인증, 데이터 모델링, 배포 자동화, 로그 분석처럼 서비스의 신뢰도를 좌우하는 영역에 관심이 많습니다.",
						"코드가 서버에서 오래 살아남는 방식을 고민합니다.",
						"이 페이지는 이력서의 정적 복사본이 아니라, 프로젝트와 운영 데이터, 글, 실험 기능이 계속 붙을 수 있는 개인 기술 플랫폼의 시작점입니다.",
						strengths()
				),
				skillCategories(),
				experiences(),
				contact()
		);
	}

	@Override
	public ProjectPageResponse getProject() {
		return new ProjectPageResponse(projects(), labFeatures());
	}

	@Override
	public ServicePageResponse getService() {
		List<ServiceResponse> services = monitoringQueryService.getMonitoringServices().services();
		return new ServicePageResponse(services, mediaItems(), contact());
	}

	private List<CtaLinkResponse> ctaLinks() {
		return List.of(
				new CtaLinkResponse("GitHub", GITHUB_URL, "primary", true),
				new CtaLinkResponse("Blog", BLOG_URL, "primary", true),
				new CtaLinkResponse("Monitoring", "/monitoring", "primary", null),
				new CtaLinkResponse("LLM Service", "/service", "primary", null)
		);
	}

	private ContactResponse contact() {
		return new ContactResponse(
				EMAIL,
				GITHUB_URL,
				BLOG_URL,
				List.of(
						new ContactLinkResponse("LinkedIn", LINKEDIN_URL, "경력과 네트워크 프로필", true),
						new ContactLinkResponse("Email", "mailto:" + EMAIL, "협업, 채용, 기술 대화 제안", null),
						new ContactLinkResponse("GitHub", GITHUB_URL, "코드, 실험, 운영 자동화 기록", true),
						new ContactLinkResponse("Blog", BLOG_URL, "트러블슈팅과 백엔드 학습 노트", true)
				)
		);
	}

	private List<NavigationCardResponse> navigationCards() {
		return List.of(
				new NavigationCardResponse("About", "/about", "Detailed profile and skills"),
				new NavigationCardResponse("LLM Service", "/service", "Local GPU LLM system overview"),
				new NavigationCardResponse("Monitoring", "/monitoring", "Infrastructure and service status"),
				new NavigationCardResponse("Project", "/project", "Project archive"),
				new NavigationCardResponse("Blog", "/blog", "Markdown or API powered posts")
		);
	}

	private List<StrengthResponse> strengths() {
		return List.of(
				new StrengthResponse("운영 관점의 설계", "로그, 장애 대응, 배포 이후의 추적 가능성을 고려해 기능을 설계합니다."),
				new StrengthResponse("데이터 중심 문제 해결", "Oracle, PostgreSQL, MariaDB 등 다양한 DB 환경에서 쿼리와 모델을 다듬습니다."),
				new StrengthResponse("인증과 보안 흐름", "세션, JWT, 권한 모델, API 보호 흐름을 서비스 구조에 맞게 정리합니다."),
				new StrengthResponse("자동화와 관측성", "Docker, Jenkins, Prometheus, Loki 기반의 운영 자동화를 확장합니다.")
		);
	}

	private List<SkillCategoryResponse> skillCategories() {
		return List.of(
				new SkillCategoryResponse("backend", "Backend", "서비스 핵심 로직, API, 트랜잭션 경계를 설계하는 주력 영역", List.of(
						new SkillItemResponse("Java", "core", List.of("language")),
						new SkillItemResponse("Spring Boot", "core", List.of("framework")),
						new SkillItemResponse("JPA", "working", List.of("orm")),
						new SkillItemResponse("MyBatis", "working", List.of("sql-mapper")),
						new SkillItemResponse("REST API", "core", List.of("api"))
				)),
				new SkillCategoryResponse("database", "Database", "운영 데이터와 쿼리 성능을 함께 고려하는 데이터 계층", List.of(
						new SkillItemResponse("Oracle", "core", null),
						new SkillItemResponse("PostgreSQL", "working", null),
						new SkillItemResponse("Redis", "working", null)
				)),
				new SkillCategoryResponse("infra", "Infra / DevOps", "배포, 프록시, 로그, 모니터링까지 잇는 운영 기반", List.of(
						new SkillItemResponse("Docker", "core", null),
						new SkillItemResponse("Nginx", "working", null),
						new SkillItemResponse("Prometheus", "learning", null)
				))
		);
	}

	private List<ExperienceResponse> experiences() {
		return List.of(
				new ExperienceResponse("ops", "운영 장애 대응", "장애 원인 추적을 위해 로그, 배포 이력, DB 상태를 함께 확인하는 흐름을 선호합니다.", "Ongoing", "Operations", List.of("logging", "incident", "observability")),
				new ExperienceResponse("db", "DB 마이그레이션", "스키마 변경, 데이터 검증, 롤백 기준을 체크리스트로 관리하며 안정성을 확보합니다.", "Project based", "Database", List.of("oracle", "postgresql", "migration")),
				new ExperienceResponse("automation", "자동화와 모니터링", "반복 배포와 상태 확인을 자동화하고, 지표 기반으로 운영 판단을 돕는 구조를 만듭니다.", "Expanding", "Infra", List.of("jenkins", "docker", "metrics"))
		);
	}

	private List<ProjectItemResponse> projects() {
		return List.of(
				new ProjectItemResponse("ops-console", "Operations Console", "운영 로그, 배포 상태, 장애 기록을 한곳에서 확인하기 위한 내부 운영 콘솔 컨셉입니다.", List.of("Spring Boot", "React", "PostgreSQL", "Docker"), List.of("운영 이벤트 조회 흐름 설계", "서버 상태 카드 기반 UI 구성", "API 응답 구조 표준화"), List.of(new ProjectLinkResponse("Case Note", "#")), true),
				new ProjectItemResponse("home-lab", "Home Lab Monitoring", "개인 서버, reverse proxy, API 서비스를 관측하는 홈랩 모니터링 초기 구성입니다.", List.of("Nginx", "Prometheus", "Loki", "Docker"), List.of("서비스 상태 데이터 모델링", "가용률 지표 설계", "알림 연동 준비"), List.of(new ProjectLinkResponse("Status", "/monitoring")), true),
				new ProjectItemResponse("local-llm", "Local LLM Control", "로컬 GPU 서버 Wake 및 Chat API 연동을 목표로 하는 실험 기능입니다.", List.of("Spring Boot", "Wake-on-LAN", "Local LLM"), List.of("GPU 서버 상태 모델링", "Wake 요청 API 설계", "채팅 API 계약 구성"), List.of(new ProjectLinkResponse("LLM", "/llm")), false)
		);
	}

	private List<LabFeatureResponse> labFeatures() {
		return List.of(
				new LabFeatureResponse("local-llm", "Local LLM Workspace", "개인 문서, 코드, 운영 로그를 검색하고 요약하는 로컬 모델 UI", "planned", List.of("llm", "rag", "privacy"), null),
				new LabFeatureResponse("infra-monitor", "Infra Monitoring API", "서버 상태 데이터를 실시간 API로 연결하고 알림 정책을 붙이는 기능", "building", List.of("status", "prometheus", "alert"), null),
				new LabFeatureResponse("automation", "Ops Automation", "배포, 백업, 점검 루틴을 자동화하고 실행 결과를 기록하는 실험", "building", List.of("jenkins", "backup", "jobs"), null)
		);
	}

	private List<ContentItemResponse> mediaItems() {
		return List.of(
				new ContentItemResponse("ops-video-note", "홈랩 모니터링 구축 로그", "서버 상태, 가용률, 응답 시간 데이터를 개인 홈페이지와 연결하는 과정을 기록할 예정입니다.", "video", "#", "2026-05-01", "YouTube Draft", List.of("home-lab", "monitoring"), true),
				new ContentItemResponse("notion-architecture", "개인 기술 플랫폼 아키텍처 보드", "Notion 또는 FigJam으로 관리할 수 있는 백엔드 플랫폼 확장 로드맵입니다.", "external-link", "#", "2026-04-12", "Notion", List.of("architecture", "roadmap"), false)
		);
	}
}
