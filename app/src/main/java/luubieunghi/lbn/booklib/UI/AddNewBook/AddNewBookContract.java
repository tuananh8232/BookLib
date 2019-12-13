package luubieunghi.lbn.booklib.UI.AddNewBook;

import android.content.Context;

import java.time.LocalDate;

import luubieunghi.lbn.booklib.Model.Book.Book;

public interface AddNewBookContract {
    interface AddNewBookMVPView{
        void BookAddedSuccess();
        void BookAddedFailure();
    }
    interface AddNewBookMVPPresenter{
        void AddBook();
        void LoadLanguages();
        void LoadServerList();
    }
}
