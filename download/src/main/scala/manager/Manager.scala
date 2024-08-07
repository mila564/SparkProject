package manager

import data._
import Getters._
import manager.UnfoldIterator.Op

object Manager {

  def apply(): Iterator[(Match, List[Profile])] = {

    List("GM", "WGM", "IM", "WIM", "FM", "WFM", "CM", "WCM")
      .iterator
      .flatMap(t => {
        println("Getting " + t + " titled players...")
        getTitledPlayers(t)
      })
      .flatMap(p => {
        println("Obtaining " + p + " tournaments... ")
        getPlayerTournaments(p)
      })
      .flatMap(r => {
        println("Obtaining from " + r + " round's info... ")
        getRounds(r)
      })
      .flatMap(g => {
        println("Getting from " + g + " group's info... ")
        getGroups(g)
      })
      .flatMap(getMatch)
      .unfold2(Set[String]())(getPlayers)


  }

}

