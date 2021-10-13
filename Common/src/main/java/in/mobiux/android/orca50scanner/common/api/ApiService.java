package in.mobiux.android.orca50scanner.common.api;

import in.mobiux.android.orca50scanner.common.api.model.ContactSupport;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {


    @POST(Endpoints.SEND_REQUEST)
    Call<ContactSupport> sendRequest(@Body ContactSupport support);

}
