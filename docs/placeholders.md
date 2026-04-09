---
description: All the PlaceholderAPI placeholders you need to work with CrownStoreData.
icon: percent
title: Placeholders
---

This are the plugins placeholders that work with the [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

# Ranking Placeholders
### All-Time
<table>
    <thead>
        <th>Placeholder (place) represents the ranking.</th>
        <th>Description</th>
    </thead>
    <tbody>
        <tr>
            <td>%ctd_alltime_amount_(place)%</td>
            <td>Returns how much the player at the specified all-time leaderboard rank has spent.</td>
        </tr>
        <tr>
            <td>%ctd_alltime_name_(place)%</td>
            <td>Returns the name of the player at the specified all-time leaderboard rank.</td>
        </tr>
        <tr>
            <td>%ctd_alltime_skin_(place)%</td>
            <td>Returns the skin of the player at the specified all-time leaderboard rank. If no skin is available, the <code>embed-purchase-offline-skin</code> value is used instead.</td>
        </tr>
    </tbody>
</table>

### Monthly
<table>
    <thead>
        <th>Placeholder (place) represents the ranking.</th>
        <th>Description</th>
    </thead>
    <tbody>
        <tr>
            <td>%ctd_monthly_amount_(place)%</td>
            <td>Returns how much the player at the specified monthly leaderboard rank has spent.</td>
        </tr>
        <tr>
            <td>%ctd_monthly_name_(place)%</td>
            <td>Returns the name of the player at the specified monthly leaderboard rank.</td>
        </tr>
        <tr>
            <td>%ctd_monthly_skin_(place)%</td>
            <td>Returns the skin of the player at the specified monthly leaderboard rank. If no skin is available, the <code>embed-purchase-offline-skin</code> value is used instead.</td>
        </tr>
    </tbody>
</table>

# Player Placeolders
<table>
    <thead>
        <tr>
            <th width="376">Placeholder</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>%ctd_player_spent_alltime%</td>
            <td>Returns the total amount the player has spent across all time.</td>
        </tr>
        <tr>
            <td>%ctd_player_spent_monthly%</td>
            <td>Returns the total amount the player has spent during the current month.</td>
        </tr>
        <tr>
            <td>%ctd_player_spent_alltime_raw%</td>
            <td>Returns the raw all-time amount the player has spent without formatting.</td>
        </tr>
        <tr>
            <td>%ctd_player_spent_monthly_raw%</td>
            <td>Returns the raw monthly amount the player has spent without formatting.</td>
        </tr>
    </tbody>
</table>

# Goal Placeholders
<table>
    <thead>
        <tr>
            <th width="376">Placeholder (goalid) represents the goal id.</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>%ctd_goal_bars_(goalid)%</td>
            <td>Returns the progress bar display for the specified goal, where <code>(goalid)</code> is the goal identifier.</td>
        </tr>
        <tr>
            <td>%ctd_goal_dcbars_(goalid)%</td>
            <td>Returns the Discord-formatted progress bar display for the specified goal, where <code>(goalid)</code> is the goal identifier.</td>
        </tr>
        <tr>
            <td>%ctd_goal_reached_amount_(goalid)%</td>
            <td>Returns the current progress amount of the specified goal.</td>
        </tr>
        <tr>
            <td>%ctd_goal_reached_percentage_(goalid)%</td>
            <td>Returns the current completion percentage of the specified goal.</td>
        </tr>
        <tr>
            <td>%ctd_goal_target_(goalid)%</td>
            <td>Returns the target amount required to complete the specified goal.</td>
        </tr>
        <tr>
            <td>%ctd_goal_achieved%</td>
            <td>Returns the number of times the goal has been completed.</td>
        </tr>
    </tbody>
</table>

## Where do i find my goalid ?

> When on goal-mode: `external`. The goal-id is provided by the store provider. Once loaded into CSD, you will find the goal-id under /csd listgoals. 

> When on goal-mode: `internal`. You have to create a goal first. Use the /csd creategoal command.

# Sale Placeholders
<table>
    <thead>
        <tr>
            <th width="376">Placeholder</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>%ctd_sales_name%</td>
            <td>Returns the name of the most recently active sale.</td>
        </tr>
        <tr>
            <td>%ctd_sales_amount%</td>
            <td>Returns the percentage or fixed value of the most recently active sale.</td>
        </tr>
    </tbody>
</table>


# Latest Purchase Placeholders
<table>
    <thead>
        <tr>
            <th width="376">Placeholder</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>%ctd_latest_player%</td>
            <td>Returns the name of the player who made the most recent purchase.</td>
        </tr>
        <tr>
            <td>%ctd_latest_amount%</td>
            <td>Returns the amount of the most recent purchase.</td>
        </tr>
        <tr>
            <td>%ctd_latest_package%</td>
            <td>Returns the name of the first package included in the most recent purchase.</td>
        </tr>
        <tr>
            <td>%ctd_latest_packages%</td>
            <td>Returns the names of all packages included in the most recent purchase.</td>
        </tr>
    </tbody>
</table>
