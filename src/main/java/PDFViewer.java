import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFViewer extends Application {

    private int currentPage = 0;
    private PDDocument document;
    private PDFRenderer pdfRenderer;

    @Override
    public void start(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран. Закрытие приложения.", ButtonType.OK);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK) {
                System.exit(0);
            }
        }

        try {
            document = PDDocument.load(selectedFile);
            pdfRenderer = new PDFRenderer(document);
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке PDF файла: " + e.getMessage());
            return;
        }

        BorderPane root = new BorderPane();
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        root.setCenter(imageView);

        showPage(imageView, currentPage);

        Scene scene = new Scene(root, 800, 600);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                showPage(imageView, currentPage - 1);
            } else if (event.getCode() == KeyCode.RIGHT) {
                showPage(imageView, currentPage + 1);
            }
        });

        scene.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                showPage(imageView, currentPage - 1);
            } else if (event.getDeltaY() < 0) {
                showPage(imageView, currentPage + 1);
            }
        });

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> adjustImageViewSize(imageView, primaryStage));
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> adjustImageViewSize(imageView, primaryStage));

        primaryStage.setScene(scene);
        primaryStage.setTitle("PDF Viewer");
        primaryStage.show();

        adjustImageViewSize(imageView, primaryStage);
    }

    private void showPage(ImageView imageView, int pageIndex) {
        try {
            if (pageIndex < 0) pageIndex = 0;
            if (pageIndex >= document.getNumberOfPages()) pageIndex = document.getNumberOfPages() - 1;

            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 150);
            imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
            currentPage = pageIndex;
        } catch (IOException e) {
            System.err.println("Ошибка при отображении страницы: " + e.getMessage());
        }
    }


    private void adjustImageViewSize(ImageView imageView, Stage stage) {
        double newWidth = stage.getWidth() - 20; // Подстраиваем ширину с небольшим отступом
        double newHeight = stage.getHeight() - 50; // Подстраиваем высоту с учетом возможного отступа для кнопок и заголовка

        imageView.setFitWidth(newWidth);
        imageView.setFitHeight(newHeight);
    }


    @Override
    public void stop() throws Exception {
        if (document != null) {
            document.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
