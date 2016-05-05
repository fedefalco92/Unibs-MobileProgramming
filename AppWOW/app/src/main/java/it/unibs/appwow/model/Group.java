package it.unibs.appwow.model;

/**
 * Created by Massi on 05/05/2016.
 */
public class Group {
    public long id;
    public String groupName;
    public String photoUri;
    public long createdAt;
    public long updatedAt;
    public long idAdmin;

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(groupName);
        return builder.toString();
    }
}
