package reserve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import system.DateByPlaceMaster;
import system.ReserveDateController;
import system.Yoyakukun;

public class MainSystemOpenSchool {

    public static void main(String[] args) throws InterruptedException {
        //インスタンスを生成

        //引数の中に以下の項目を番号で設定
        //利用場所(要素0):スポーツ屋外->01,スポーツ屋内->02,学校開放（屋内）->03,学校開放（屋外）->04
        //利用目的(要素1):屋外サッカー->004,サロンフットボール・フットサル->029,サロンフットボール・フットサル->064,サッカー->052,
        //地域(要素2):指定なし（初期値:"札幌市")
        //施設名称:
        Yoyakukun yoyaku01 = new Yoyakukun
                ("学校開放（屋内）", "サロンフットボール・フットサル", "札幌市", null, "2021/02/01", "2021/02/27",null);
        //割り当て↑("-施設区分-",    "-利用目的-",      "-札幌市（固定）-","-施設名称[null]","-検索範囲開始日-","-検索範囲終了日-")
        String reserveMonth = "2"; //<-月を指定
        DateByPlaceMaster school01 = new DateByPlaceMaster("", "", "", "", ""); //
        DateByPlaceMaster school02 = new DateByPlaceMaster("27", "", "", "", "");
        DateByPlaceMaster school03 = new DateByPlaceMaster("2", "6", "9", "13", "16");
        DateByPlaceMaster school04 = new DateByPlaceMaster("2", "9", "", "", "");

        List<String> places = new ArrayList<>();
        Collections.addAll(places, "新琴似小","手稲山口小","新陵中","屯田北小");  //,<-ここに予約したい施設名を追加*138行目以降のschoolの個数と確認
        try {

            //ChoromeDriverをセット＆定義
            System.setProperty("webdriver.chrome.driver", "./exe/chromedriver");
            WebDriver driver = new ChromeDriver();
            //指定したdriverに対して最大で10秒間待つように設定する
            WebDriverWait wait = new WebDriverWait(driver, 10);
            //札幌市施設予約のページを開く
            driver.get("https://www.city.sapporo.jp/shisetsu-yoyaku/");
            WebElement element = wait
                    .until(ExpectedConditions.visibilityOfElementLocated(By.linkText("札幌市公共施設予約情報システムへ"))); //リンクテキスト名が"札幌市公共施設予約情報システムへ"の要素を取得
            element.click();//"札幌市公共施設予約情報システムへ"のリンクをクリック

            //エクセルファイルへアクセスしてID番号を取得する--------------------------------------
            Workbook excel;
            excel = WorkbookFactory
                    .create(new File("/Users/yamamotokouhei/Documents/Selenium_Java/ReserveDataSeparated.xlsx"));//Excelfileにアクセス
            Sheet sheet = excel.getSheet("sheet2");// <--ここでシート名を指定**(自分の担当はsheet2)**
            for (int i = 1; i <= 25; i++) { //<----エクセルの範囲指定はここ！！1~26までの数字
                Row rowC = sheet.getRow(i); //行を読み込み
                Cell cellId = rowC.getCell(2); //Cellを指定(ここは固定)
                String id = cellId.getStringCellValue(); //指定した場所の文字列を取得
                Row rowD = sheet.getRow(i); //行を読み込み
                Cell cellPass = rowD.getCell(3); //Cellを指定(ここは固定)
                String pass = cellPass.getStringCellValue(); //指定した場所の文字列を取得
                System.out.println("No." + i); //件数
                System.out.println("ID:" + id + "/Password:" + pass); //取得したデータを出力
                if (id == null || id.equals("")) {
                    System.out.println("ExcelFileにデータがありません");
                    break;
                }

                //ログイン画面を開く--------------------------------------------------------
                //                Thread.sleep(3000);//3秒待機

                JavascriptExecutor jse = (JavascriptExecutor) driver;
                jse.executeScript("window.scrollBy(0,500)", "");//500px下にスクロール
                WebElement element01 = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_ContentPlaceHolder1_btnMenuLogin"))); //施設予約ログインを探す
                element01.click(); //"施設予約ログイン"のリンクをクリック
                //ログイン画面でID/Passwordを入力する
                WebElement element02 = wait.until(ExpectedConditions
                        .visibilityOfElementLocated(By.id("ctl00_ContentPlaceHolder1_txtRiyoushaID"))); //指定された要素(検索テキストボックス)が表示状態になるまで待機する
                element02.sendKeys(id);//IDボックスにエクセルから取得したID入力する
                WebElement element03 = driver.findElement(By.id("ctl00_ContentPlaceHolder1_txtPassword"));
                element03.sendKeys(pass);//エクセルから取得したPasswordを入力
                WebElement element04 = driver.findElement(By.name("ctl00$ContentPlaceHolder1$btnLogin"));
                element04.click();//"ログインをクリック"

                for (String place : places) {
                    yoyaku01.setPlaceName(place);
                    if(yoyaku01.getPlaceName() == null || yoyaku01.getPlaceName().equals("")){
                        int noPlace = places.indexOf("")+1;
                        System.out.println("会場リストの" + noPlace + "番目に会場の指定がありません");
                    }else if(yoyaku01.getPlaceName() == "屯田北小" || yoyaku01.getPlaceName().equals("屯田北小")) {
                        yoyaku01.setUsePurpose("トレーニング");
                    }else{
                        yoyaku01.setUsePurpose("サロンフットボール・フットサル");
                    }
                //メインメニュー画面で施設予約検索をクリック
                    jse.executeScript("window.scrollBy(0,500)", "");//500px下にスクロール
                    Thread.sleep(1500);
                    WebElement element05 = driver.findElement(By.name("ctl00$ContentPlaceHolder1$btnMenuShinseiSrch"));
                    element05.click();//施設予約検索をクリック
                //施設の予約検索オペレーション--------------------------------------------------------------------
                    driver.navigate().refresh(); //ページをリフレッシュ
                    Thread.sleep(1000);//1秒待機
                    jse.executeScript("window.scrollBy(0,300)", "");//500px下にスクロール
                    Select dropdown01 = new Select(
                            driver.findElement(
                                    By.name("ctl00$ContentPlaceHolder1$ShinseiKumiawaseInp1$drpPurposeBunrui")));
                    dropdown01.selectByVisibleText(yoyaku01.getUsePlace());//施設区分の選択
                    Select dropdown02 = new Select(
                            driver.findElement(By.name("ctl00$ContentPlaceHolder1$ShinseiKumiawaseInp1$drpPurpose")));
                    dropdown02.selectByVisibleText(yoyaku01.getUsePurpose());//利用目的の選択
                    WebElement element06 = driver
                            .findElement(By.name("ctl00$ContentPlaceHolder1$ShinseiKumiawaseInp1$txtShisetsuMeisho"));
                    element06.clear();//すでに文字が入っている場合のため、クリア処理
                    element06.sendKeys(yoyaku01.getPlaceName());//施設名を入力する
                    WebElement element07 = driver
                            .findElement(By.name("ctl00$ContentPlaceHolder1$ShinseiKumiawaseInp1$txtFromDate"));
                    element07.sendKeys(yoyaku01.getStartDate());//検索範囲の開始日を入力する
                    WebElement element08 = driver
                            .findElement(By.name("ctl00$ContentPlaceHolder1$ShinseiKumiawaseInp1$txtToDate"));
                    element08.sendKeys(yoyaku01.getEndDate());//検索範囲の終了日を入力する
                    Thread.sleep(1500);//3秒待機
                    WebElement element09 = driver
                            .findElement(By.name("ctl00$ContentPlaceHolder1$ShinseiKumiawaseInp1$btnSearch"));
                    element09.click();//検索をクリック
                    //カレンダーが表示されて予約する日にちを指定--------------------------------------------------------------------------
                    List<String> reserveDays = new ArrayList<>();
                    if (yoyaku01.getPlaceName() == "新琴似小") {
                        Collections.addAll(reserveDays,school01.getReserveDate01(),school01.getReserveDate02(),
                                school01.getReserveDate03(),school01.getReserveDate04(),
                                school01.getReserveDate05());//DateByPlaceクラスからリストに日にちを格納
                    }
                    if (yoyaku01.getPlaceName() == "手稲山口小") {
                        Collections.addAll(reserveDays,school02.getReserveDate01(),school02.getReserveDate02(),
                                school02.getReserveDate03(),school02.getReserveDate04(),
                                school02.getReserveDate05());//DateByPlaceクラスからリストに日にちを格納
                    }
                    if (yoyaku01.getPlaceName() == "新陵中") {
                        Collections.addAll(reserveDays,school03.getReserveDate01(),school03.getReserveDate02(),
                                school03.getReserveDate03(),school03.getReserveDate04(),
                                school03.getReserveDate05());//DateByPlaceクラスからリストに日にちを格納
                    }
                    if (yoyaku01.getPlaceName() == "屯田北小") {
                        Collections.addAll(reserveDays,school04.getReserveDate01(),school04.getReserveDate02(),
                                school04.getReserveDate03(),school04.getReserveDate04(),
                                school04.getReserveDate05());//DateByPlaceクラスからリストに日にちを格納
                    }

                    //ここから繰り返し処理
                    for (String reserveDay : reserveDays) {
                        if(reserveDay.equals("") || reserveDay == null){
                            System.out.println("日にちの指定がありません");
                            break;
                        }

                        jse.executeScript("window.scrollBy(0,300)", "");//300px下にスクロール
                        Thread.sleep(2000);
                        WebElement element10 = driver
                                .findElement(
                                        By.xpath("//a[contains(@title,'" + reserveMonth + "月" + reserveDay + "日')]"));
                        element10.click();//カレンダー上の日付をクリック
                        //時間帯を指定------------------------------------------------------------------------
                        jse.executeScript("window.scrollBy(0,500)", "");//500px下にスクロール
                        //日付から曜日を取得して曜日によって指定する時間帯を変える　曜日の取得　土日は13時から・平日は18時から
                        ReserveDateController rdc = new ReserveDateController(); //曜日取得のオブジェクトを生成
                        String youbi = rdc.getYoubi("2021", reserveMonth, reserveDay); //曜日を取得する
                         if(yoyaku01.getPlaceName() =="新陵中" || yoyaku01.getPlaceName().equals("新陵中")){
                             WebElement elementShinryouchu = driver.findElement(By.id("ctl00_ContentPlaceHolder1_JikantaiSel0"));
                             elementShinryouchu.click();//時間帯を指定
                        }else if (youbi.equals("日曜") || youbi.equals("土曜")) { //土曜日日曜日の時
                            WebElement element11a = driver.findElement(By.id("ctl00_ContentPlaceHolder1_JikantaiSel3"));
                            element11a.click();//時間帯を指定　
                        } else { //平日の時id:ctl00_ContentPlaceHolder1_JikantaiSel0
                            WebElement element11b = driver.findElement(By.id("ctl00_ContentPlaceHolder1_JikantaiSel0"));
                            element11b.click();//時間帯を指定
                        }
                        WebElement element12 = driver.findElement(By.id("ctl00_ContentPlaceHolder1_btnShinseiCnf"));
                        element12.click();//申請
                        //申し込み申請確認----------------------------------------------------------------------
                        jse.executeScript("window.scrollBy(0,600)", "");//600px下にスクロール
                        WebElement element13 = driver.findElement(By.name("ctl00$ContentPlaceHolder1$btnShinsei"));
                        element13.click();//予約完了
                        System.out.println(yoyaku01.getPlaceName() + "/" + reserveMonth + "月" + reserveDay + "日" + youbi + "をID"
                                + id + "で予約しました。");
                        //”別の日を指定して予約する”ボタンをクリックする
                        jse.executeScript("window.scrollBy(0,300)", "");//600px下にスクロール
                        WebElement otherdate = driver.findElement(By.id("ctl00_ContentPlaceHolder1_btnDateYoyaku"));
                        otherdate.click();//別日程を検索
                    }
                    jse.executeScript("window.scrollBy(0,300)", "");//600px下にスクロール
                    //メニューに戻って別の施設の予約をする
                    WebElement backToMenu = driver.findElement(By.linkText("メニューへ戻る"));
                    backToMenu.click();//メニューへ戻る
                }

                Thread.sleep(1500);
                //ログアウト---------------------------------------------------------
                jse.executeScript("window.scrollBy(0,-600)", "");//600px上にスクロール
                WebElement logout = driver.findElement(By.name("ctl00$btnLogout"));
                logout.click();//ログアウトして次のID番号へ繰り返し

            }
            driver.quit();

        } catch (EncryptedDocumentException | IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
