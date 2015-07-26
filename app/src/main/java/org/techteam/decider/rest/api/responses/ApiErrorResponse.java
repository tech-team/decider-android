package org.techteam.decider.rest.api.responses;

import android.os.Parcel;

public abstract class ApiErrorResponse extends ApiResponse {

    public enum ErrorType {
        NONE,
        GENERIC,
        SERVER
    }

    public enum GenericErrorCode {
        INVALID_TOKEN,
        SERVER_ERROR,
        NO_INTERNET,
        INTERNAL_PROBLEMS;

        private static GenericErrorCode[] cachedValues = values();

        public static GenericErrorCode fromOrdinal(int i) {
            return cachedValues[i];
        }
    }

    public enum ServerErrorCode {
        REGISTRATION_UNFINISHED(3000),
        INVALID_CREDENTIALS(7002),
        USERNAME_REQUIRED(7003),
        USERNAME_TAKEN(7006),
        ALREADY_VOTED(8200)
        ;

        private static ServerErrorCode[] cachedValues = values();

        int code;

        ServerErrorCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ServerErrorCode fromCode(int code) {
            for (ServerErrorCode errorCode : cachedValues) {
                if (errorCode.code == code) {
                    return errorCode;
                }
            }
            throw new RuntimeException(String.format("Unknown ServerErrorCode (%d)", code));
        }
    }

    private ErrorType errorType;
    private GenericErrorCode genericErrorCode;
    private ServerErrorCode serverErrorCode;



    public ApiErrorResponse(String errorMsg) {
        super(true, errorMsg);
        this.errorType = ErrorType.NONE;
    }

    public ApiErrorResponse(GenericErrorCode code) {
        this((String) null);
        this.errorType = ErrorType.GENERIC;
        this.genericErrorCode = code;
    }

    public ApiErrorResponse(ServerErrorCode code) {
        this((String) null);
        this.errorType = ErrorType.SERVER;
        this.serverErrorCode = code;
    }

    protected ApiErrorResponse(Parcel in) {
        super(in);
        errorType = ErrorType.valueOf(in.readString());
        genericErrorCode = GenericErrorCode.fromOrdinal(in.readInt());
        serverErrorCode = ServerErrorCode.fromCode(in.readInt());
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public GenericErrorCode getGenericErrorCode() {
        return genericErrorCode;
    }

    public ServerErrorCode getServerErrorCode() {
        return serverErrorCode;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(errorType.toString());
        dest.writeInt(genericErrorCode.ordinal());
        dest.writeInt(serverErrorCode.getCode());
    }
}
