package walerowicz.pawel.SpotifyFun;

public record ApiCallProblem(
        String message
) {
    public static ApiCallProblem fromException(Exception exception) {
        return new ApiCallProblem(exception.getMessage());
    }
}
