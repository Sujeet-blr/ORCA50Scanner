package in.mobiux.android.orca50scanner.api.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SUJEET KUMAR on 29-Mar-21.
 */

@Entity(tableName = "laboratory")
public class Laboratory extends BaseModel {

    @PrimaryKey
    @NonNull
    @SerializedName("labId")
    @Expose
    private int labId;

    @SerializedName("labName")
    @Expose
    private String labName;

    @SerializedName("levelId")
    @Expose
    private int levelId;

    @SerializedName("levelName")
    @Expose
    private String levelName;

    @SerializedName("department")
    @Expose
    private int department;

    @Ignore
    @SerializedName("assets")
    @Expose
    private List<AssetHistory> assets = new ArrayList<>();

    public int getLabId() {
        return labId;
    }

    public void setLabId(int labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getDepartment() {
        return department;
    }

    public void setDepartment(int department) {
        this.department = department;
    }

    public List<AssetHistory> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetHistory> assets) {
        this.assets = assets;
    }
}
