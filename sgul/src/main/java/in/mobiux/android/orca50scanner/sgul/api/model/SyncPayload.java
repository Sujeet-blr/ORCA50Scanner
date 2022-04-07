package in.mobiux.android.orca50scanner.sgul.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SUJEET KUMAR on 15-Jul-21.
 */
public class SyncPayload extends BaseModel{

    @SerializedName("assets")
    @Expose
    private List<AssetHistory> histories = new ArrayList<>();

    public List<AssetHistory> getHistories() {
        return histories;
    }

    public void setHistories(List<AssetHistory> histories) {
        this.histories = histories;
    }
}
