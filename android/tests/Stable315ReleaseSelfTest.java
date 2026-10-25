package dev.bennett.codexmeter;
import java.util.*;import java.nio.file.*;import org.json.*;
public final class Stable315ReleaseSelfTest {
    static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    static JSONObject release(String tag,String asset)throws Exception{
        String base=GitHubReleaseSource.REPOSITORY_URL;
        return new JSONObject().put("tag_name",tag).put("prerelease",tag.startsWith("beta-")).put("html_url",base+"/releases/tag/"+tag).put("assets",new JSONArray().put(new JSONObject().put("name",asset).put("size",24800000).put("browser_download_url",base+"/releases/download/"+tag+"/"+asset)).put(new JSONObject().put("name","SHA256SUMS.txt").put("browser_download_url",base+"/releases/download/"+tag+"/SHA256SUMS.txt")));
    }
    public static void main(String[] args)throws Exception{
        JSONObject next=release("v3.1.5","CodexMeter-3.1.5-ko.apk");
        List<GitHubRelease> rows=GitHubReleaseParser.parse(new JSONArray().put(release("v3.1.4","CodexMeter-3.1.4-ko.apk")).put(next).put(release("beta-v9.9.15","GPT-HUD-BETA-9.9.15.apk")).toString());
        check(rows.size()==2,"Stable parser excludes beta-only assets");
        for(String v:new String[]{"3.1.0","3.1.1","3.1.2","3.1.3","3.1.4"})for(String c:new String[]{"stable","alpha"})check(UpdateChannel.selectUpdate(rows,v,c).version.equals("3.1.5"),"Previous stable discovers3.1.5");
        for(String c:new String[]{"stable","alpha"})check(UpdateChannel.selectUpdate(rows,"3.1.5",c)==null,"Current version not reoffered");
        check(KoreanUpdateTrust.compatible("dev.bennett.codexmeter",KoreanUpdateTrust.CERTIFICATE,91,92),"Original stable package and signer advance");
        check(!KoreanUpdateTrust.compatible("dev.bennett.codexmeter.trial",KoreanUpdateTrust.CERTIFICATE,91,990015),"Beta package cannot overwrite stable");
        check(!KoreanUpdateTrust.compatible("dev.bennett.codexmeter","unknown",91,92),"Wrong signing key refused");
        check(!KoreanUpdateTrust.compatible("dev.bennett.codexmeter",KoreanUpdateTrust.CERTIFICATE,92,91),"Downgrade refused");
        check(GitHubReleaseParser.parse(new JSONArray().put(new JSONObject(next.toString()).put("draft",true)).toString()).isEmpty(),"Draft not offered");
        JSONObject hostile=new JSONObject(next.toString());hostile.getJSONArray("assets").getJSONObject(0).put("browser_download_url","https://github.com/evil/repo/releases/download/v3.1.5/app.apk");check(GitHubReleaseParser.parse(new JSONArray().put(hostile).toString()).isEmpty(),"Foreign asset rejected");
        if(args.length==1){List<GitHubRelease> live=GitHubReleaseParser.parse(Files.readString(Path.of(args[0])));for(String v:new String[]{"3.1.2","3.1.3","3.1.4"})check(UpdateChannel.selectUpdate(live,v,"stable").version.equals("3.1.5"),"Actual public discovery "+v);check(UpdateChannel.selectUpdate(live,"3.1.5","stable")==null,"Public current version not reoffered");}
        System.out.println("Stable315 release: "+checks+" checks passed");
    }
}
