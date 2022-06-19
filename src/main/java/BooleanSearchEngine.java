import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> index = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        //Идем по всем файлам директории
        for (File pdf : pdfsDir.listFiles()) {
            if (pdf.isFile()) {
                PdfDocument doc = new PdfDocument(new PdfReader(pdf));
                int pageCount = doc.getNumberOfPages();
                //Идем по всем страницам файла
                for (int i = 1; i <= pageCount; i++) {
                    //Получаем весь текст файла и разбиваем его по словам
                    PdfPage page = doc.getPage(i);
                    String textFromPage = PdfTextExtractor.getTextFromPage(page);
                    String[] wordsFromPage = textFromPage.split("\\P{IsAlphabetic}+");

                    //Считаем частоту появления каждого слова среди найденных на странице
                    Map<String, Integer> freqs = new HashMap<>();
                    for (var word : wordsFromPage) {
                        if (word.isEmpty()) {
                            continue;
                        }
                        freqs.put(word.toLowerCase(), freqs.getOrDefault(word.toLowerCase(), 0) + 1);
                    }

                    //Для каждого уникального слова со страницы сосздаем PageEntry,
                    //записав туда имя файла, номер страницы и частоту встречи слова
                    for (String uniqueWord : freqs.keySet()) {
                        int wordFreq = freqs.get(uniqueWord);
                        PageEntry pageEntry = new PageEntry(pdf.getName(), i, wordFreq);

                        //Чтобы сохранить результат, вытаскиваем из итоговой мапы текущий набор PageEntry по слову
                        //и добавляем в него только что созданный PageEntry.
                        //Если набора PageEntry по слову не было в итоговой мапе, то создаем его
                        List<PageEntry> tempPageEntries = index.get(uniqueWord);
                        if (tempPageEntries == null) {
                            tempPageEntries = new ArrayList<>();
                        }
                        tempPageEntries.add(pageEntry);

                        //Акуальный набор PageEntry по слову устанивливаем в мапу.
                        index.put(uniqueWord, tempPageEntries);
                    }
                }
            }
        }


    }

    @Override
    public List<PageEntry> search(String word) {
        //Из итоговой мапы PageEntry ищем набор по слову и возвращаем его отсортировав по частотности вхождения слов.

        List<PageEntry> result = index.get(word);

        if (result != null) {
            result = result.stream()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        }
        return result;
    }
}
