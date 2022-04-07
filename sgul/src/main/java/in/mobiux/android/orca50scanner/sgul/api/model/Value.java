package in.mobiux.android.orca50scanner.sgul.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Value extends BaseModel{

    @SerializedName("choice")
    @Expose
    private String choice;

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }
}
