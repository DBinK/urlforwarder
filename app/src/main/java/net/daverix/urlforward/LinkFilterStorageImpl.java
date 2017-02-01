package net.daverix.urlforward;


import net.daverix.urlforward.db.DatabaseObservable;
import net.daverix.urlforward.db.UrlForwarderContract.UrlFilters;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static net.daverix.urlforward.Constants.TABLE_FILTER;

class LinkFilterStorageImpl implements LinkFilterStorage {
    private final DatabaseObservable database;
    private final LinkFilterMapper mapper;

    public LinkFilterStorageImpl(DatabaseObservable database,
                                 LinkFilterMapper mapper) {
        this.database = database;
        this.mapper = mapper;
    }

    @Override
    public Completable insert(LinkFilter linkFilter) {
        return Observable.just(linkFilter)
                .map(mapper::getValues)
                .flatMapCompletable(values -> database.insert(TABLE_FILTER, values)
                        .toCompletable());
    }

    @Override
    public Completable update(LinkFilter linkFilter) {
        return Observable.just(linkFilter)
                .map(mapper::getValues)
                .flatMapCompletable(values -> database.update(TABLE_FILTER,
                        values,
                        UrlFilters._ID + "=?",
                        getIdArgs(linkFilter.getId()))
                        .toCompletable());
    }

    @Override
    public Completable delete(long id) {
        return Completable.defer(() -> database.delete(TABLE_FILTER,
                UrlFilters._ID + "=?",
                getIdArgs(id)).toCompletable());
    }

    private String[] getIdArgs(long id) {
        return new String[]{String.valueOf(id)};
    }

    @Override
    public Single<LinkFilter> getFilter(long id) {
        return database.query(TABLE_FILTER,
                mapper.getColumns(),
                String.format("%s=?", UrlFilters._ID),
                getIdArgs(id))
                .map(mapper::mapFilter)
                .singleOrError();
    }

    @Override
    public Observable<LinkFilter> queryAll() {
        return database.query(TABLE_FILTER, mapper.getColumns(), null, null)
                .map(mapper::mapFilter);
    }
}
