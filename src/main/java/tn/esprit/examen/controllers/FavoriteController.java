package tn.esprit.examen.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Favorite;
import tn.esprit.examen.services.FavoriteService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/add")
    public ResponseEntity<Favorite> addFavorite(@RequestParam Long candidateId, @RequestParam Long jobOfferId) {
        return ResponseEntity.ok(favoriteService.addFavorite(candidateId, jobOfferId));
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeFavorite(@RequestParam Long candidateId, @RequestParam Long jobOfferId) {
        favoriteService.removeFavorite(candidateId, jobOfferId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/byCandidate/{candidateId}")
    public ResponseEntity<List<Favorite>> getFavoritesByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(favoriteService.getFavoritesByCandidate(candidateId));
    }
}
