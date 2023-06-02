package models
class Boardgame (
    var id: Int,
    var title: String,
    var originalTitle: String,
    var yearPublished: Int,
    var image: String,
    var thumbnail: String,
    var bggId: Int,
    var minPlayers: Int,
    var maxPlayers: Int,
    var playingTime: Int
)
