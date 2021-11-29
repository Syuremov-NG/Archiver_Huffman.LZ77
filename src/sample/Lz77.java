package sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Lz77 {

    public static String compress(String path) throws IOException {
        List<String> stringList;
        String compressedString = "";
        Tuple thisTuple;
        File inputFile = new File(path);
        StringBuilder rawData = new StringBuilder();
        String searchSubstring; // Подстрока окна поиска
        int matchLen; // Длина найденной строки
        int matchLoc; // Индекс найденной строки
        int charCnt = 0; // Кол-во пройденных символов
        int searchWindowStart; // Начало соваря
        int searchWindowLen = 9; //Размер словаря
        int lookAheadWindowLen = 9; //Размер буффера

        FileInputStream fs = new FileInputStream(String.valueOf(Paths.get(inputFile.getAbsolutePath())));
        byte[] buffer = new byte[fs.available()];
        fs.read(buffer, 0, fs.available());
        rawData.append(new String(buffer));

        while (charCnt < rawData.length()) {
            //Находим начало словаря.
            searchWindowStart = Math.max(charCnt - searchWindowLen, 0);
            //Получаем подстроку словаря для поиска совпадения
            if (charCnt == 0) {
                //Если словарь пуст
                searchSubstring = "";
            } else {
                searchSubstring = rawData.substring(searchWindowStart, charCnt);
            }
            //Ищем в словаре соответствие следующему символу
            matchLen = 1;
            String searchTarget = rawData.substring(
                    charCnt, charCnt + matchLen);

            // Если словарь содержит символ
            if (searchSubstring.contains(searchTarget)) {

                matchLen++; // Увеличиваем длину искомой строки

                // Проверяем, пока длина искомой строки не превысит длину буффера
                while (matchLen <= lookAheadWindowLen) {

                    // Ищем в словаре соответствие
                    searchTarget = rawData.substring(charCnt, charCnt + matchLen);
                    matchLoc = searchSubstring.indexOf(searchTarget);

                    // Если строка найдена и сумма (пройденные символы + длина совпадающей строки) меньше длины текста
                    // Увеличим длину совпадающей строки
                    if ((matchLoc != -1) && ((charCnt + matchLen) < rawData.length())) {
                        matchLen++;
                    } else {
                        // Иначе выходить из while
                        break;
                    }
                }
                // Уменьшим значение сопадающей строки до достоверного
                matchLen--;

                // Вернем значение начала совпадающей строки
                matchLoc = searchSubstring.indexOf(
                        rawData.substring(charCnt, charCnt + matchLen));

                // Увеличиваем счетчик символов, чтобы внешний цикл пропускал совпадающие символы.
                charCnt += matchLen;

                // Добавляем закодированный элемент
                //1. Смещение расстояния к месту совпадения в словаре
                //2. Длина совпадения
                //3. Первый несовпадающий символ

                // Подсчет смещения, где был найден символ в словаре
                int offset = (charCnt < (searchWindowLen + matchLen)) ? charCnt - matchLoc - matchLen : searchWindowLen - matchLoc;
                // Сохраняем первый несовпавший символ
                String nextChar = rawData.substring(charCnt, charCnt + 1);

                thisTuple = new Tuple(offset, matchLen, nextChar);
                compressedString += thisTuple;
            } else {
                // Если не нашлось совпадений
                //1. 0
                //2. 0
                //3. Несовпавший символ
                String nextChar = rawData.substring(
                        charCnt, charCnt + 1);
                thisTuple = new Tuple(0, 0, nextChar);
                compressedString += thisTuple;
            }
            // Увеличили кол-во пройденных символов
            charCnt++;

        }
        return compressedString;
    }

    public static String decompress(String s) throws IOException {

        ArrayList<Tuple> data = new ArrayList<>();
        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; i += 3) {
            data.add(new Tuple(Integer.parseInt(String.valueOf(chars[i])), Integer.parseInt(String.valueOf(chars[i + 1])), String.valueOf(chars[i + 2])));
        }

        // Восстановленное сообщение
        StringBuffer reconData = new StringBuffer();

        //Итератор по кортежам
        Iterator<Tuple> iterator = data.iterator();

        //Основной цикл
        while (iterator.hasNext()) {
            Tuple nextTuple = iterator.next();
            if (nextTuple.stringLen == 0) {
                // Не совпавший ни с чем символ
                reconData.append(nextTuple.nextChar);
            } else {
                // Кортеж заполненный инфой о совпадающем участке
                // Читаем символы совпадающей строки
                for (int i = 0; i < nextTuple.stringLen; i++) {
                    char workingChar = reconData.charAt(reconData.length() - nextTuple.offset);
                    // С каждой итерацией длина reconData увеличивается
                    reconData.append(workingChar);
                }
                // Так же добавим несовпадающий символ из кортежа
                reconData.append(nextTuple.nextChar);
            }
        }
        return reconData.toString();
    }

}