package in.mobiux.android.orca50scanner.otsmobile.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetails extends BaseModel {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("clockNumber")
    @Expose
    private String clockNumber;
    @SerializedName("pinNumber")
    @Expose
    private String pinNumber;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("profilePicture")
    @Expose
    private String profilePicture;
    private UserDetailsGroup[] groups;
    private UserDetailRoles[] roles;
    private int teamId;
    private String[] claims;
    private int ref = 0;

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("body")
    @Expose
    private UserDetails body;

    public UserDetails getBody() {
        return body;
    }

    public void setBody(UserDetails body) {
        this.body = body;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClockNumber() {
        return clockNumber;
    }

    public void setClockNumber(String clockNumber) {
        this.clockNumber = clockNumber;
    }

    public String getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public UserDetailsGroup[] getGroups() {
        return groups;
    }

    public void setGroups(UserDetailsGroup[] groups) {
        this.groups = groups;
    }

    public UserDetailRoles[] getRoles() {
        return roles;
    }

    public void setRoles(UserDetailRoles[] roles) {
        this.roles = roles;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String[] getClaims() {
        return claims;
    }

    public void setClaims(String[] claims) {
        this.claims = claims;
    }

    public int getRef() {
        return ref;
    }

    public void setRef(int ref) {
        this.ref = ref;
    }
}
