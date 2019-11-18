package jade.util;

//#PJAVA_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE

public interface Callback<Result> {
	void onSuccess(Result result);
	void onFailure(Throwable throwable);
}
