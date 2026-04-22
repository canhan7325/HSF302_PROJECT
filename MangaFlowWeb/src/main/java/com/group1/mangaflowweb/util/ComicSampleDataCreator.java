//package com.group1.mangaflowweb.util;
//
//import com.group1.mangaflowweb.entity.*;
//import com.group1.mangaflowweb.enums.ComicEnum;
//import com.group1.mangaflowweb.repository.*;
//import org.springframework.boot.SpringApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
///**
// * Utility class để tạo sample data cho Comics
// * Chạy hàm main này một lần để tạo sample comics với genres và users
// * Sau đó xóa file này hoặc comment lại hàm main
// */
//public class ComicSampleDataCreator {
//
//    public static void main(String[] args) {
//        ConfigurableApplicationContext context = SpringApplication.run(
//            new Class[]{com.group1.mangaflowweb.MangaFlowWebApplication.class},
//            args
//        );
//
//        UserRepository userRepository = context.getBean(UserRepository.class);
//        GenreRepository genreRepository = context.getBean(GenreRepository.class);
//        ComicRepository comicRepository = context.getBean(ComicRepository.class);
//
//        try {
//            // ... existing code ...
//
//            // Step 1: Create or get admin user
//            Users adminUser = userRepository.findByUsername("admin").orElse(null);
//            if (adminUser == null) {
//                System.out.println("✗ Admin user not found! Please run AdminUserCreator first!");
//                context.close();
//                System.exit(1);
//            }
//            System.out.println("✓ Admin user found!");
//
//            // ... existing code ...
//
//            // Step 2: Create sample genres
//            List<Genres> genres = new ArrayList<>();
//            String[] genreNames = {"Action", "Adventure", "Comedy", "Drama", "Fantasy", "Horror", "Romance", "Sci-Fi"};
//
//            for (String genreName : genreNames) {
//                Optional<Genres> existingGenre = genreRepository.findByName(genreName);
//                if (existingGenre.isEmpty()) {
//                    Genres genre = Genres.builder()
//                            .name(genreName)
//                            .slug(genreName.toLowerCase().replace(" ", "-"))
//                            .build();
//                    genres.add(genreRepository.save(genre));
//                } else {
//                    genres.add(existingGenre.get());
//                }
//            }
//            System.out.println("✓ Genres ready! (Total: " + genres.size() + ")");
//
//            // Step 3: Create sample comics
//            Object[][] comicData = {
//                    {"One Piece", "one-piece", "Follow Luffy's journey to become Pirate King", 5432},
//                    {"Naruto", "naruto", "Ninja adventures and friendship bonds", 4321},
//                    {"Dragon Ball", "dragon-ball", "Epic battles and martial arts", 3210},
//                    {"Attack on Titan", "attack-on-titan", "Fight against giants in a walled world", 2876},
//                    {"Demon Slayer", "demon-slayer", "Battle against demons to save humanity", 4156},
//                    {"My Hero Academia", "my-hero-academia", "Superhero academy adventure", 3654},
//                    {"Jujutsu Kaisen", "jujutsu-kaisen", "Cursed spirits and exorcism", 3456},
//                    {"Bleach", "bleach", "Soul reapers and afterlife battles", 2234}
//            };
//
//            int comicsCreated = 0;
//            for (Object[] data : comicData) {
//                String slug = (String) data[1];
//                Optional<Comics> existing = comicRepository.findBySlug(slug);
//                if (existing.isEmpty()) {
//                    // Re-fetch genres to avoid detached entity issue
//                    List<Genres> attachedGenres = new ArrayList<>();
//                    for (Genres genre : genres.subList(0, Math.min(3, genres.size()))) {
//                        attachedGenres.add(genreRepository.findByName(genre.getName()).orElse(genre));
//                    }
//
//                    // Save comic WITHOUT genres first
//                    Comics comic = Comics.builder()
//                            .title((String) data[0])
//                            .slug(slug)
//                            .description((String) data[2])
//                            .coverImg("https://via.placeholder.com/300x400?text=" + data[0])
//                            .status(ComicEnum.ONGOING)
//                            .viewCount((Integer) data[3])
//                            .user(adminUser)
//                            .createdAt(LocalDateTime.now().minusDays((long) (Math.random() * 100)))
//                            .build();
//                    comic = comicRepository.save(comic);
//
//                    // Then set genres and save again (MERGE operation)
//                    comic.addGenreComics(attachedGenres);
//                    comicRepository.save(comic);
//
//                    comicsCreated++;
//                }
//            }
//
//            System.out.println("✓ Comics sample data created!");
//            System.out.println("  - Comics created: " + comicsCreated);
//            System.out.println("  - Total comics in DB: " + comicRepository.count());
//            System.out.println("\n⚠️  Vui lòng xóa file này hoặc comment lại hàm main sau khi tạo xong!");
//
//        } catch (Exception e) {
//            System.out.println("✗ Lỗi khi tạo sample data: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            context.close();
//            System.exit(0);
//        }
//    }
//}
//
