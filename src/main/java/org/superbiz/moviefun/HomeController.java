package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;

    @Autowired
    @Qualifier("moviesPTM")
    private PlatformTransactionManager moviesPTM;

    @Autowired
    @Qualifier("albumsPTM")
    private PlatformTransactionManager albumsPTM;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(moviesPTM);
        transactionTemplate.execute(
                new TransactionCallback<Object>(){
                    @Override
                    public Object doInTransaction(TransactionStatus transactionStatus) {
                        for (Movie movie : movieFixtures.load()) {
                            moviesBean.addMovie(movie);
                        }

                        return null;
                    }
                }
        );

        transactionTemplate = new TransactionTemplate(this.albumsPTM);
        transactionTemplate.execute(
                new TransactionCallback<Object>(){
                    @Override
                    public Object doInTransaction(TransactionStatus transactionStatus) {
                        for (Album album : albumFixtures.load()) {
                            albumsBean.addAlbum(album);
                        }
                        return null;
                    }
                }
        );

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
