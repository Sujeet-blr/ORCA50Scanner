package in.mobiux.android.orca50scanner.sgul.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SUJEET KUMAR on 28-Mar-21.
 */
public class AssetResponse extends BaseModel {

    @Expose
    @SerializedName("assigned")
    private boolean assigned;
    @Expose
    @SerializedName("department")
    private Department department;
//    @Expose
//    @SerializedName("asset_attributes")
//    private List<String> assetAttributes;
    @Expose
    @SerializedName("asset_id")
    private AssetId assetId;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("asset_type")
    private AssetType assetType;
    @Expose
    @SerializedName("id")
    private int id;
    @SerializedName("asset_attributes")
    @Expose
    private List<AssetAttribute> assetAttributes = new ArrayList<>();



    public boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<AssetAttribute> getAssetAttributes() {
        return assetAttributes;
    }

    public void setAssetAttributes(List<AssetAttribute> assetAttributes) {
        this.assetAttributes = assetAttributes;
    }

    public AssetId getAssetId() {
        return assetId;
    }

    public void setAssetId(AssetId assetId) {
        this.assetId = assetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static class Department extends BaseModel {
        @Expose
        @SerializedName("name")
        private String name;
        @Expose
        @SerializedName("id")
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class AssetId extends BaseModel {
        @Expose
        @SerializedName("qr_code")
        private String qrCode;
        @Expose
        @SerializedName("bar_code")
        private String barCode;
        @Expose
        @SerializedName("rfid")
        private String rfid;
        @Expose
        @SerializedName("asset_fp_id")
        private String assetFpId;

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }

        public String getBarCode() {
            return barCode;
        }

        public void setBarCode(String barCode) {
            this.barCode = barCode;
        }

        public String getRfid() {
            return rfid;
        }

        public void setRfid(String rfid) {
            this.rfid = rfid;
        }

        public String getAssetFpId() {
            return assetFpId;
        }

        public void setAssetFpId(String assetFpId) {
            this.assetFpId = assetFpId;
        }
    }

    public static class AssetType extends BaseModel {
        @Expose
        @SerializedName("name")
        private String name;
        @Expose
        @SerializedName("id")
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
