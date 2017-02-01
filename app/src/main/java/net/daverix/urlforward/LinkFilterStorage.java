package net.daverix.urlforward;


import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface LinkFilterStorage {
    Completable insert(LinkFilter linkFilter);

    Completable update(LinkFilter linkFilter);

    Completable delete(long id);

    Single<LinkFilter> getFilter(long id);

    Observable<LinkFilter> queryAll();
}
