package im.fdx.v2ex.network.cookie;

import java.util.Collection;
import java.util.List;

import okhttp3.Cookie;

/**
 * Created by fdx on 2017/3/16.
 */

public interface CookiePersistor {

    void removeAll(List<Cookie> cookies);

    void clear();

    void persist(Cookie cookie);

    void persistAll(Collection<Cookie> cookies);

    List<Cookie> loadAll();


}
