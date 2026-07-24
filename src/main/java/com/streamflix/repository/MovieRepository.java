package com.streamflix.repository;

import com.streamflix.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("""
           select distinct m from Movie m
           join m.genres g
           where g.name in :genreNames
           """)
    List<Movie> findByGenreNames(@Param("genreNames") List<String> genreNames);

    @Query("select m from Movie m order by m.viewCount desc")
    List<Movie> findTrending(Pageable pageable);

    @Query("select m from Movie m order by m.averageRating desc")
    List<Movie> findTopRated(Pageable pageable);

    @Query("""
           select m from Movie m
           where m.id not in :excludedIds
           and exists (
               select g from m.genres g where g.name in :genreNames
           )
           order by m.averageRating desc
           """)
    List<Movie> findSimilarByGenres(@Param("genreNames") List<String> genreNames,
                                     @Param("excludedIds") List<Long> excludedIds,
                                     Pageable pageable);
}
