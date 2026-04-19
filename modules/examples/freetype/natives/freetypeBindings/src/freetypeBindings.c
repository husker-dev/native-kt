#include <api.h>
#include <stdio.h>

#include <ft2build.h>

#include FT_FREETYPE_H


void helloWorld() {
    FT_Library library;
    FT_Face face;
    FT_GlyphSlot slot;
    FT_Error error;

    error = FT_Init_FreeType(&library);
    if (error) {
        printf("FT_Init_FreeType failed\n");
        return;
    }

     const char* fontFilename = "Arial.ttf";

    error = FT_New_Face(library, fontFilename, 0, &face);
    if (error == FT_Err_Unknown_File_Format) {
        printf("FT_New_Face failed: unknown file format\n");
        return;
    }
    if (error) {
        printf("FT_New_Face failed: could not open/read font file\n");
        return;
    }

    error = FT_Set_Char_Size(face, 0, 48 * 64, 20, 20); // 48pt font size
    if (error) {
        printf("FT_Set_Char_Size failed\n");
        return;
    }


    char characterToLoad = 'X';
    error = FT_Load_Char(face, characterToLoad, FT_LOAD_RENDER);
    if (error) {
        printf("FT_Load_Char failed\n");
        return;
    }

    slot = face->glyph;
    const FT_Bitmap bitmap = slot->bitmap;

    printf("Successfully loaded glyph '%c'\n", characterToLoad);
    printf("Bitmap dimensions: %dx%d pixels\n", bitmap.width, bitmap.rows);
    printf("Bitmap buffer size (bytes): %d\n", bitmap.pitch * bitmap.rows);


    // print
    // Градиент яркости: от пустого к плотному
    const char* shades = " .:-=+*#%@";
    const int shade_count = 10;

    for (unsigned int row = 0; row < bitmap.rows; row++) {
        for (unsigned int col = 0; col < bitmap.width; col++) {
            // pitch может быть отрицательным, берём abs
            unsigned char pixel = bitmap.buffer[row * (unsigned int)abs(bitmap.pitch) + col];

            // Маппим 0–255 в индекс символа
            int idx = (pixel * (shade_count - 1)) / 255;
            printf("%c%c", shades[idx], shades[idx]); // двойной символ — глиф не квадратный
        }
        printf("\n");
    }


    FT_Done_Face(face);
    FT_Done_FreeType(library);

    fflush(stdout);
}