package org.techteam.decider.content.entities;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.techteam.decider.rest.OperationType;

@Table(name="UploadedImage", id = BaseColumns._ID)
public class UploadedImageEntry extends Model {

    @Column(name="imageUid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private String imageUid;

    @Column(name="ordinalId", index = true)
    private int ordinalId;

    public UploadedImageEntry() {
        super();
    }

    public UploadedImageEntry(String imageUid, int ordinalId) {
        super();
        this.imageUid = imageUid;
        this.ordinalId = ordinalId;
    }

    public String getImageUid() {
        return imageUid;
    }

    public static UploadedImageEntry getLatestByOrdinalId(int ordinalId) {
        return new Select().from(UploadedImageEntry.class)
                .where("ordinalId = ?", ordinalId)
                .orderBy("_id desc")
                .limit(1)
                .executeSingle();
    }

    public static void deleteAll() {
        new Delete().from(QuestionEntry.class).execute();
    }
}
