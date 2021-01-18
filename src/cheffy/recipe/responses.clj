(ns cheffy.recipe.responses
  (:require [spec-tools.data-spec :as ds]))

(def base-url "https://learnreitit.com")

(def step
  {:step/step_id     string?
   :step/sort        int?
   :step/description string?
   :step/recipe_id   string?})

(def ingredient
  {:ingredient/ingredient_id string?
   :ingredient/sort          int?
   :ingredient/name          string?
   :ingredient/amount        int?
   :ingredient/measure       string?
   :ingredient/recipe_id     string?})

(def recipe
  {:recipe/recipe_id            string?
   :recipe/prep_time            int?
   :recipe/favorite_count       int?
   :recipe/uid                  string?
   :recipe/img                  string?
   :recipe/public               boolean?
   :recipe/name                 string?
   (ds/opt :recipe/steps)       [step]
   (ds/opt :recipe/ingredients) [ingredient]})



(def recipes
  {:public          [recipe]
   (ds/opt :drafts) [recipe]})