# =============================================================================
# fit-core-backend Dockerfile (멀티스테이지, arm64/amd64 멀티아치)
# =============================================================================
#
# 런타임 필요 env (값은 런타임 주입 — 이미지에 굽지 말 것):
#   SPRING_PROFILES_ACTIVE=prod
#   DB_URL=jdbc:mariadb://<host>:<port>/<db>
#   DB_USERNAME=<user>
#   DB_PASSWORD=<password>
#   AI_BASE_URL=http://<ai-host>:<port>
#   JWT_SECRET=<128자 이상 hex>
#   CORS_ALLOWED_ORIGINS=https://<fe-domain>
#   GOOGLE_CLIENT_ID=<optional>
#   GOOGLE_CLIENT_SECRET=<optional>
#   KAKAO_CLIENT_ID=<optional>
#   KAKAO_CLIENT_SECRET=<optional>
#   NAVER_CLIENT_ID=<optional>
#   NAVER_CLIENT_SECRET=<optional>
#
# =============================================================================

# ── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Gradle wrapper + build 정의 먼저 복사 (의존성 레이어 캐시 활용)
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# 의존성 다운로드 (소스 없이 — 캐시 레이어)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q 2>/dev/null || true

# 소스 복사 후 bootJar 빌드 (테스트 제외)
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# non-root 유저로 실행 (컨테이너 보안)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

# JVM 컨테이너 메모리 인지 활성화 (기본값).
# 메모리 제한 환경(무료 클라우드)에서 OOM 방지:
#   -XX:MaxRAMPercentage=50.0  → 컨테이너 메모리의 50% 사용 (기본 25%)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=50.0", "-jar", "app.jar"]
