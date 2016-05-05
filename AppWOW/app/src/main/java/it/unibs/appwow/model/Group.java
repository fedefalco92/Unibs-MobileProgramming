package it.unibs.appwow.model;

/**
 * Created by Massi on 05/05/2016.
 */
public class Group {
    public long id;
    public String groupName;
    public int photoUri;
    public long createdAt;
    public long updatedAt;
    public long idAdmin;

    public Group(long id, String groupName, int photoUri, long createdAt, long updatedAt, long idAdmin) {
        this.id = id;
        this.groupName = groupName;
        this.photoUri = photoUri;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.idAdmin = idAdmin;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(groupName);
        return builder.toString();
    }
}
