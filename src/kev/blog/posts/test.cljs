(ns kev.blog.posts.test
  (:require
   [kev.blog.posts]
   ["@mui/material" :as mui])
  (:require-macros
   [kev.blog.posts :as posts
    :refer [defpost -]]))

(defpost
  {::posts/post-id "test-post"
   ::posts/title "First Test Post"
   ::posts/created "2022-02-14"}

  "some post content" this is more post content that is not in a bullet and should follow
  a similar style and yes. hopefully it all goes as planned.
  "some post content" this is more post content that is not in a bullet and should follow
  a similar style and yes. hopefully it all goes as planned.

  [:br]
  (- some other shite with good new stuff also with a good bit of content and other things
     some other shite with good new stuff also with a good bit of content and other things
     Also, a bit of luck includes this

     (- and this * is a sub-bullet)
     (- and another dropdown ya)
     [:br] more stuff in the toplevel bullet)
  And finally more content at the toplevel. Use with your own discretion as there are no
  guarentees that this will work at all.)

(defpost
  {::posts/post-id "test-post2"
   ::posts/title "Second Test Post"
   ::posts/created "2022-02-15"}

  --This is another post. I WILL REMOVE WHEN REAL POSTS!
  This one has only text.
  [:> mui/Box {:component "span" :sx {:bgcolor "warning.light"}} " this might be highlighted "]
  This is another post. This one has only text.
  This is another post. This one has only text.
  This is another post. This one has only text.
  This is another post. This one has only text.
  This is another post. This one has only text.
  And some content finally at the end. ofc.
  [:b " taco "] and othershite
  )
