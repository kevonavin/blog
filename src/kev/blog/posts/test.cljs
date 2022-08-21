(ns kev.blog.posts.test
  (:require
   [kev.blog.posts :refer [indent link highlight]]
   ["@mui/material" :as mui])
  (:require-macros
   [kev.blog.posts :as posts
    :refer [defpost -]]))

(defpost
  {::posts/post-id "demo-post"
   ::posts/title "Demo Post"
   ::posts/created "2022-02-14"}

  This is demo text "you can use quotes or symbos"

  And whitespace "  doesn't matter    "


  (- here's a list)
  (- at the top level
     (- and here is a nested one)
     [:br]
     (- that was a line break))
  (- and another list item)

  [:br]
  (indent) You have to indent for new paragraphs. And put in a "[:br]." Oh and (link "this" "https://google.com")
  is a link! And for something (highlight "this is highlighted") text.
  Can also do [:b "bold text"] .

  )
