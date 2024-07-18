package com.melodify.Melodify.Services;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.huggingface.translator.TextClassificationTranslator;
import ai.djl.huggingface.translator.TextClassificationTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.training.util.ProgressBar;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;

@Service
public class SentimentAnalysisService {

    private static final String MODEL_URL = "djl://ai.djl.huggingface.pytorch/distilbert-base-uncased-finetuned-sst-2-english";

    public String analyzeSentiment(String text) throws IOException, ModelException, TranslateException {
        Criteria<String, Classifications> criteria = Criteria.builder()
                .setTypes(String.class, Classifications.class)
                .optModelUrls(MODEL_URL)
                .optTranslatorFactory(new TextClassificationTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();

        try (ZooModel<String, Classifications> model = criteria.loadModel();
             Predictor<String, Classifications> predictor = model.newPredictor()) {
            Classifications result = predictor.predict(text);
            return result.best().getClassName();
        }
    }
}
//Pretend you're a sentiment analysis for song lyrics. Your options are 