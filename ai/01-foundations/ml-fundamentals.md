# Machine Learning Fundamentals

The base every AI engineer should own — even in the LLM era, because RAG, evals, embeddings and fine-tuning all rest on these ideas.

## The core idea

Instead of writing rules, you **show examples and let an algorithm learn the mapping** from input to output. The learned function generalizes to new, unseen data — that's the whole game.

## The three learning types

| Type | You give it | It learns | Example |
|---|---|---|---|
| **Supervised** | Inputs + correct labels | Input → output mapping | Spam detection, price prediction |
| **Unsupervised** | Inputs only | Structure/patterns | Customer segmentation, anomaly detection |
| **Reinforcement** | Environment + rewards | A policy of actions | Game AI, robotics, RLHF for LLMs |

Most business ML is **supervised**. LLM alignment uses **reinforcement** (RLHF).

## Supervised learning: the two flavors

- **Classification** → predict a category (spam/not-spam, which digit, churn/stay).
- **Regression** → predict a number (house price, tomorrow's demand).

## The workflow (this is 80% of real ML work)

```
1. Define the problem + success metric
2. Collect & clean data          ← most of the time goes here
3. Feature engineering           ← often what actually decides success
4. Split: train / validation / test
5. Train a model
6. Evaluate on held-out data
7. Tune (hyperparameters, features)
8. Deploy + monitor (data drift!)
```

## The cardinal sin: overfitting

- **Overfitting**: model memorizes training data, fails on new data (low train error, high test error). Too complex / too little data.
- **Underfitting**: model too simple to capture the pattern (high error everywhere).
- **The fix**: always evaluate on a **held-out test set the model never saw**; use regularization, more data, or a simpler model.

> This is exactly why [LLM evals](evals-and-testing.md) insist on a held-out set — same principle, one level up.

## Train / validation / test — why three splits

- **Train**: the model learns on it.
- **Validation**: you tune hyperparameters against it.
- **Test**: touched **once**, at the end, to estimate real-world performance. Peek at it during development and your numbers become fiction.

## Evaluation metrics (pick the right one)

For classification, **accuracy lies** on imbalanced data (99% "not fraud" by always guessing "not fraud"):

| Metric | Answers |
|---|---|
| **Precision** | Of predicted positives, how many were right? (cost of false alarms) |
| **Recall** | Of actual positives, how many did we catch? (cost of misses) |
| **F1** | Balance of precision & recall |
| **AUC-ROC** | Ranking quality across thresholds |

For regression: **MAE**, **RMSE**, **R²**. Choose based on what a mistake actually costs.

## Key concepts you'll keep meeting

- **Features**: the input variables. Good features often beat fancy models.
- **Bias–variance tradeoff**: too simple (bias) vs too sensitive (variance) — you tune between them.
- **Cross-validation**: rotate the validation split to use data efficiently.
- **Embeddings**: dense vectors that capture meaning — the bridge from classic ML to [LLMs/RAG](../02-rag-architectures/rag-concepts.md). See also the [tokenization & embeddings deep dive](tokenization-and-embeddings.md) in this folder.

## Where classic ML still wins over LLMs

- Structured/tabular data (fraud scoring, forecasting, recommendations) → gradient boosting (XGBoost/LightGBM) often beats everything.
- Tight latency/cost budgets, need for interpretability, or huge volumes.
- LLMs shine on **unstructured language**; classic ML shines on **numbers and tables**. Know which problem you have.

## Practical starting stack

- **Python** + **scikit-learn** (classic ML), **pandas** (data), **matplotlib** ([dataviz](../../resources/cheatsheets/sql.md) mindset).
- Deep learning: **PyTorch** (research/industry default).
- Golden habit: get a dumb baseline working end-to-end first, then improve — a deployed simple model beats a perfect notebook.

## MLOps in one breath

Models rot: the world changes, so **data drift** degrades them. Production ML needs versioning (data + model), monitoring of live performance, and retraining pipelines — the [DevSecOps](../../devsecops/README.md) mindset applied to models.
