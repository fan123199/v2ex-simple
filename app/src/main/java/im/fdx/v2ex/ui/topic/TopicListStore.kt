package im.fdx.v2ex.ui.topic

import im.fdx.v2ex.data.model.Topic

object TopicListStore {
    var currentTopics: List<Topic> = emptyList()

    fun setTopics(topics: List<Topic>) {
        currentTopics = topics
    }
}
