package com.greycodes.qtunes;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyService extends Service {
    DownloadManager downloadManager;
    String s1,s2,url,results,durl,downloadlink;
    private long downloadReference;
    int id;
    static int count=0;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        s1=intent.getStringExtra("song1");
        s2=intent.getStringExtra("song2");
       url="https://devru-raaga-v1.p.mashape.com/json/search-v2.asp?c=500&l=H&p=1&q="+s1+"&mashape-key=STPFzI5Z96msh8BRoQDf1SutiDeap1sKZvUjsnkKkgZee22lcJ";
     //  url="https://devru-raaga-v1.p.mashape.com/json/search-v2.asp?c=500&l=H&p=1&q=Rangoli&mashape-key=STPFzI5Z96msh8BRoQDf1SutiDeap1sKZvUjsnkKkgZee22lcJ";
        new FindSongId().execute(url);
        url=url.replaceAll(" ", "%20");

        return super.onStartCommand(intent, flags, startId);
    }

    class FindSongId extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params)  {
            // TODO Auto-generated method stub

            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-type","application/json");
            InputStream inputstream = null;
            try{
                org.apache.http.HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity =  response.getEntity();
                inputstream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream,"UTF-8"),8);
                StringBuilder theStringBuilder = new StringBuilder();
                String line = null;
                while((line= reader.readLine())!=null){
                    theStringBuilder.append(line+ '\n');

                }
                results = theStringBuilder.toString();

            }catch(Exception e){
                stopSelf();
            }finally{
                try{
                    if(inputstream!=null)
                        inputstream.close();
                }catch(Exception e){
                    stopSelf();
                }

            }
            return results;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject =new JSONObject(results);
            JSONArray songs= jsonObject.getJSONObject("songs").getJSONArray("albums");

                if(songs.length()>0){
                  id=  songs.getJSONObject(0).getInt("id");
                    durl="https://devru-raaga-v1.p.mashape.com/json/playlist.asp?hls=1&id="+id+"&mashape-key=STPFzI5Z96msh8BRoQDf1SutiDeap1sKZvUjsnkKkgZee22lcJ";
                    durl= durl.replaceAll(" ", "%20");
                    new FindSongUrl().execute(durl);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }


    class FindSongUrl extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params)  {
            // TODO Auto-generated method stub

            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost(durl);
            httppost.setHeader("Content-type","application/json");
            InputStream inputstream = null;
            try{
                org.apache.http.HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity =  response.getEntity();
                inputstream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream,"UTF-8"),8);
                StringBuilder theStringBuilder = new StringBuilder();
                String line = null;
                while((line= reader.readLine())!=null){
                    theStringBuilder.append(line+ '\n');

                }
                results = theStringBuilder.toString();

            }catch(Exception e){
                stopSelf();
            }finally{
                try{
                    if(inputstream!=null)
                        inputstream.close();
                }catch(Exception e){
                    stopSelf();
                }

            }
            return results;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                count++;
                JSONObject jsonObject =new JSONObject(results);
               downloadlink=jsonObject.getJSONArray("playlist").getJSONObject(0).getString("url");

                DownloadManager mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request mRqRequest = new DownloadManager.Request(
                        Uri.parse(downloadlink));
                mRqRequest.setDescription("This is Test File");
//             mRqRequest.setDestinationUri(Uri.parse("give your local path"));
                long idDownLoad=mManager.enqueue(mRqRequest);
                if (count<2) {
                    url="https://devru-raaga-v1.p.mashape.com/json/search-v2.asp?c=500&l=H&p=1&q="+s2+"&mashape-key=STPFzI5Z96msh8BRoQDf1SutiDeap1sKZvUjsnkKkgZee22lcJ";
                    url= url.replaceAll(" ", "%20");
                    new FindSongId().execute(url);
                }else{
                    stopSelf();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
