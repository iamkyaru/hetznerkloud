package tech.sco.hetznerkloud

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpStatusCode
import tech.sco.hetznerkloud.model.ErrorCode
import tech.sco.hetznerkloud.model.InvalidInputError
import tech.sco.hetznerkloud.model.RateLimitExceededError
import tech.sco.hetznerkloud.model.ResourceLimitExceededError
import tech.sco.hetznerkloud.model.UnauthorizedError
import tech.sco.hetznerkloud.model.UniquenessError
import java.time.Instant

private const val TEST_TOKEN = "foo"

class CloudApiErrorTest :
    ShouldSpec({

        val apiToken = ApiToken(TEST_TOKEN)

        should("handle unauthorized error response") {
            val mockEngine = createMockEngine(apiToken)
            val underTest = CloudApiClient.of(ApiToken("invalid token"), mockEngine)

            try {
                underTest.servers.all()
            } catch (f: Failure) {
                f.error shouldBe UnauthorizedError("Request was made with an invalid or unknown token")
                f.request shouldNotBe null
            }
        }

        should("handle rate limit error response") {
            val mockEngine = createErrorEngine(ErrorCode.RATE_LIMIT_EXCEEDED, HttpStatusCode.TooManyRequests)

            val underTest = CloudApiClient.of(apiToken, mockEngine)

            try {
                underTest.servers.all()
            } catch (f: Failure) {
                f.error.errorCode shouldBe ErrorCode.RATE_LIMIT_EXCEEDED
                val rateLimitError = f.error as RateLimitExceededError
                rateLimitError.hourlyRateLimit shouldBe 3600
                rateLimitError.hourlyRateLimitRemaining shouldBe 2456
                rateLimitError.hourlyRateLimitReset shouldBe Instant.ofEpochSecond(1731011315)
                f.request shouldNotBe null
            }
        }

        should("handle uniqueness error response") {
            val mockEngine = createErrorEngine(ErrorCode.UNIQUENESS_ERROR, HttpStatusCode.UnprocessableEntity)

            val underTest = CloudApiClient.of(apiToken, mockEngine)

            try {
                underTest.servers.all()
            } catch (f: Failure) {
                f.error.errorCode shouldBe ErrorCode.UNIQUENESS_ERROR
                val uniquenessError = f.error as UniquenessError
                uniquenessError.details shouldBe UniquenessError.Details(
                    fields = listOf(
                        UniquenessError.Details.Field(name = "public_key"),
                    ),
                )
                f.request shouldNotBe null
            }
        }

        should("handle resource limit exceeded error response") {
            val mockEngine = createErrorEngine(ErrorCode.RESOURCE_LIMIT_EXCEEDED, HttpStatusCode.UnprocessableEntity)

            val underTest = CloudApiClient.of(apiToken, mockEngine)

            try {
                underTest.servers.all()
            } catch (f: Failure) {
                f.error.errorCode shouldBe ErrorCode.RESOURCE_LIMIT_EXCEEDED
                val resourceLimitExceededError = f.error as ResourceLimitExceededError
                resourceLimitExceededError.details shouldBe ResourceLimitExceededError.Details(
                    limits = listOf(
                        ResourceLimitExceededError.Details.Field(name = "project_limit"),
                    ),
                )
                f.request shouldNotBe null
            }
        }

        should("handle invalid input response") {
            val mockEngine = createErrorEngine(ErrorCode.INVALID_INPUT, HttpStatusCode.UnprocessableEntity)

            val underTest = CloudApiClient.of(apiToken, mockEngine)

            try {
                underTest.servers.all()
            } catch (f: Failure) {
                f.error.errorCode shouldBe ErrorCode.INVALID_INPUT
                val invalidInputError = f.error as InvalidInputError
                invalidInputError.details shouldBe InvalidInputError.Details(
                    fields = listOf(
                        InvalidInputError.Details.Field("broken_field", listOf("is too long")),
                    ),
                )
                f.request shouldNotBe null
            }
        }

        should("handle other error responses") {
            forAll(
                row(ErrorCode.FORBIDDEN, HttpStatusCode.Forbidden),
                row(ErrorCode.SERVER_ERROR, HttpStatusCode.InternalServerError),
                row(ErrorCode.SERVICE_ERROR, HttpStatusCode.InternalServerError),
                row(ErrorCode.NOT_FOUND, HttpStatusCode.NotFound),
                row(ErrorCode.JSON_ERROR, HttpStatusCode.BadRequest),
                row(ErrorCode.METHOD_NOT_ALLOWED, HttpStatusCode.MethodNotAllowed),
            ) { errorCode, statusCode ->

                val mockEngine = createErrorEngine(errorCode, statusCode)

                val underTest = CloudApiClient.of(apiToken, mockEngine)

                try {
                    underTest.servers.all()
                } catch (f: Failure) {
                    f.error.errorCode shouldBe errorCode
                    f.request shouldNotBe null
                }
            }
        }
    })
