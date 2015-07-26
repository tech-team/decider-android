package org.techteam.decider.rest.api.CategoriesGet;

import android.os.Parcel;
import android.os.Parcelable;

import org.techteam.decider.rest.api.responses.ApiOkResponse;

public class CategoriesGetOkResponse extends ApiOkResponse {

    private int count = 0;

    public CategoriesGetOkResponse() {
        super();
    }

    protected CategoriesGetOkResponse(Parcel in) {
        super(in);
        count = in.readInt();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(count);
    }

    public static final Parcelable.Creator<CategoriesGetOkResponse> CREATOR
            = new Parcelable.Creator<CategoriesGetOkResponse>() {
        public CategoriesGetOkResponse createFromParcel(Parcel in) {
            return new CategoriesGetOkResponse(in);
        }

        public CategoriesGetOkResponse[] newArray(int size) {
            return new CategoriesGetOkResponse[size];
        }
    };
}
