(ns kev.blog.posts.ekata-reflections
  (:require
   [kev.blog.posts :refer [link indent highlight br]]
   ["@mui/material" :as mui])
  (:require-macros
   [kev.blog.posts :as posts
    :refer [defpost -]]))

(defpost
  {::posts/post-id "ekata-reflections"
   ::posts/title "Ekata Reflections"
   ::posts/created "2022-08-19"}

  (highlight "this is a work in progress. read this at your own risk")
  (br)

  I was reading some
  (link "reflections" "https://vickiboykis.com/2022/07/25/looking-back-at-two-years-at-automattic-and-tumblr/")
  and wanted to make my own about Ekata.
  I loved working there, and it was a necessary
  learning experience. But I'll complain.

  (br)
  You can't be motivated only by cool technology or learning or career
  "development," and you'll regret trying. You need something else. There's too
  high an opportunity cost to do something that doesn't feel meaningful.
  (br)
  Where can one look to find meaning? I'm convinced there's not a well-defined
  (link "answer" "https://meaningness.com/misunderstanding-meaningness-makes-many-miserable").
  But I'm hoping there's at least some guidelines.
  (br)
  pain complaints\:
  (- Largs organizations systematically destroy individual ownership of code.
     Tasks should ideally be less than three days, and anyone can pick up any
     task, so it was really hard to do something meaningful. Only by breaking
     these rules were seniors able to do anything significant.  There was also a
     huge legacy codebase built on top of a bunch of very poorly written macros
     that no one understands and was afraid to touch.  This makes it impossible
     to (link "hold a program in your head" "http://paulgraham.com/head.html")
     and therefore impossible to write top-notch code or maximally enjoy coding
     "(though you can still have some fun)" \. This is
     probably why lisps are not used very much in large organizations.
     (br)
     One of my favorite work memories was when\, due to a tight deadline\, it was
     just me and one other dev building and designing a whole transaction processing
     app. The actual app was relatively boring, but it had to scale
     to thousands of qps, and we designed & built it in a month from scratch.
     So maybe there's some meaning in the lack of organization.)
  (- It felt more exciting before we got bought bc everyone owned stock. The VP
     of engineering was more involved with on-call and fighting fires, and the
     customer experience seemed more urgent.)
  (- Nothing wrong with the company, but an inherent aspect of the stage we were
     at made it so there wasn't much new development. Mainly just integrating with
     Mastercard to bring the same product there. I'd rather get more experience
     building new stuff.)
  #_(- I didn't feel like I was really helping anyone. I was just making the rich
     richer. And if I didn't show up, someone else would fill in seamlessly.)
  #_(- coworkers were so smart and so down to just stay. why?
     (- I know they didn't care about fraud prevention of mastercard's "\"mission\"")
     (- They loved stability?)
     (- Good money / comfort.)
     (- why did (highlight "I stay") for so long?!))
  )
