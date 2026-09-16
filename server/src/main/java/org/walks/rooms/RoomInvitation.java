package org.walks.rooms;

import io.nayuki.qrcodegen.QrCode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.walks.rooms.RoomServer.map;

/** Join-only invitations. The fragment never contains a member or administrator credential. */
final class RoomInvitation {
    static String address(String raw, boolean originOnly) {
        String clean=raw.trim();
        if(clean.length()>256 || clean.chars().anyMatch(c->c<=32 || c==127)) throw new IllegalArgumentException("请填写有效的网页版地址");
        URI uri;
        try { uri=URI.create(clean); } catch(IllegalArgumentException error) { throw new IllegalArgumentException("请填写完整的网址"); }
        if(!List.of("http","https").contains(uri.getScheme()) || uri.getHost()==null || uri.getRawUserInfo()!=null || uri.getRawQuery()!=null || uri.getRawFragment()!=null ||
            (originOnly && !List.of("","/").contains(uri.getRawPath()))) throw new IllegalArgumentException("地址只支持完整的 HTTP/HTTPS 网址，不能包含账号、查询或片段");
        return originOnly ? clean.replaceAll("/+$","") : clean;
    }
    static String origin(String web) {
        URI uri=URI.create(address(web,false));
        return uri.getScheme()+"://"+uri.getRawAuthority();
    }
    static Map<String,Object> view(String web,String api,String roomId,String invitation) {
        web=address(web,false); api=address(api,true);
        if(web.startsWith("https:") && !api.startsWith("https:")) throw new IllegalArgumentException("HTTPS 网页需使用 HTTPS 房间服务；可填写同域代理地址");
        String url=web+"#room="+roomId+"&invite="+invitation+"&server="+URLEncoder.encode(api,StandardCharsets.UTF_8);
        QrCode qr=QrCode.encodeText(url,QrCode.Ecc.MEDIUM);
        List<String> rows=new ArrayList<>();
        for(int y=0;y<qr.size;y++) {
            StringBuilder row=new StringBuilder();
            for(int x=0;x<qr.size;x++) row.append(qr.getModule(x,y)?'1':'0');
            rows.add(row.toString());
        }
        return map("url",url,"modules",rows);
    }
}
