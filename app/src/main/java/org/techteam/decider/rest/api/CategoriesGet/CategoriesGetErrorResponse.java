package org.techteam.decider.rest.api.CategoriesGet;

import android.os.Parcel;
import android.os.Parcelable;

import org.techteam.decider.rest.api.responses.ApiErrorResponse;

public class CategoriesGetErrorResponse extends ApiErrorResponse {

    public CategoriesGetErrorResponse(String errorMsg) {
        super(errorMsg);
    }

    public CategoriesGetErrorResponse(GenericErrorCode code) {
        super(code);
    }

    public CategoriesGetErrorResponse(ServerErrorCode code) {
        super(code);
    }

    protected CategoriesGetErrorResponse(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<CategoriesGetErrorResponse> CREATOR
            = new Parcelable.Creator<CategoriesGetErrorResponse>() {
        public CategoriesGetErrorResponse createFromParcel(Parcel in) {
            return new CategoriesGetErrorResponse(in);
        }

        public CategoriesGetErrorResponse[] newArray(int size) {
            return new CategoriesGetErrorResponse[size];
        }
    };
}
