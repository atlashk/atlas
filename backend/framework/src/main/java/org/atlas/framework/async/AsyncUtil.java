package org.atlas.framework.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AsyncUtil {

  public void executeAsync(AsyncTask task) {
    CompletableFuture.runAsync(task);
  }

  public static <T, R> CompletableFuture<Void> executeAsync(List<T> items, Function<T, R> task,
      Consumer<R> onSuccess, Consumer<Throwable> onError) {
    // Run each task in parallel, handle errors inline
    List<CompletableFuture<R>> futures = items.stream()
        .map(item -> CompletableFuture.supplyAsync(() -> task.apply(item))
            .whenComplete((res, ex) -> {
              if (ex != null) {
                onError.accept(ex);
              } else {
                onSuccess.accept(res);
              }
            }))
        .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  public static CompletableFuture<Void> executeAsync(List<AsyncTask> tasks) {
    // Run each task in parallel, handle errors inline
    List<CompletableFuture<Void>> futures = tasks.stream()
        .map(task -> CompletableFuture.runAsync(task)
            .whenComplete((res, ex) -> {
              if (ex != null) {
                task.onError(ex);
              } else {
                task.onSuccess();
              }
            })
        )
        .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }
}
