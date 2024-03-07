import com.example.cheap.models.LoginRequest
import com.example.cheap.models.LoginResponse
import com.example.cheap.models.Record
import com.example.cheap.models.RecordsResponse
import com.example.cheap.models.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ClientService {

    @POST("Auth/authenticate/")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("Records")
    suspend fun createRecord(@Body record: Record): Response<Record>

    @GET("Records/list/{id}")
    suspend fun listMyRecords(@Path("id") id: String): Response<RecordsResponse>;

    @POST("Auth/refresh-token")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<LoginResponse>
}
