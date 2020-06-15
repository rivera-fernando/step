// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      Collection<String> attendees = request.getAttendees();
      Collection<String> optional = request.getOptionalAttendees();
      long duration = request.getDuration();
      
      //EDGE CASES
      //return whole day if the meeting to schedule requires no attendees
      if (attendees.size() == 0) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      //return no availability if the meeting to schedule is longer than the day
      else if (duration > 24*60) {
          return Arrays.asList();
      }
      //return whole day if there are no events at all that day
      else if (events.size() == 0) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      //END EDGE CASES
      //otherwise, use algo
      else {
          ArrayList<Event> conflicting_events = new ArrayList<>();
          //retrieve all the events of attendees from the request
          for (Event event : events) {
              ArrayList<String> modifier = new ArrayList<String>(event.getAttendees());
              modifier.retainAll(request.getAttendees());
              if (modifier.size() >= 1) {
                  conflicting_events.add(event);
              }
          }
          //if there were none, return whole day
          if (conflicting_events.size() == 0) {
              return Arrays.asList(TimeRange.WHOLE_DAY);
          }
          ArrayList<Event> merged_events = mergeEvents(conflicting_events);
          return getAvailableTimes(merged_events, request);
      }
  }

  public ArrayList<Event> mergeEvents(ArrayList<Event> conflicting_events) {
      ArrayList<Event> events_contained_by_others = new ArrayList<>();
      for (Event outer_event : conflicting_events) {
          for (Event inner_event : conflicting_events) {
              if(outer_event != inner_event && outer_event.getWhen().contains(inner_event.getWhen())) {
                  events_contained_by_others.add(inner_event);
              }
          }
      }
      for (Event event: events_contained_by_others) {
          conflicting_events.remove(event);
      }
      return conflicting_events;
  }

  public Collection<TimeRange> getAvailableTimes(ArrayList<Event> merged_events, MeetingRequest request) {
      Collection<TimeRange> potential = new ArrayList<>();
      Collection<TimeRange> finalized = new ArrayList<>();
      if (merged_events.size() == 1) {
        potential.add(TimeRange.fromStartEnd(0, merged_events.get(0).getWhen().start(), false));
        potential.add(TimeRange.fromStartEnd(merged_events.get(0).getWhen().end(), 24*60, false));
      }
      else {
      
      for (int i = 0; i < merged_events.size(); i++) {
        if (i == 0) {
            potential.add(TimeRange.fromStartEnd(0, merged_events.get(i).getWhen().start(), false));
        }
        else if (i == merged_events.size() - 1) {
            potential.add(TimeRange.fromStartEnd(merged_events.get(i-1).getWhen().end(), merged_events.get(i).getWhen().start(), false));
            potential.add(TimeRange.fromStartEnd(merged_events.get(i).getWhen().end(), 24*60, false));
        }
        else {
            potential.add(TimeRange.fromStartEnd(merged_events.get(i-1).getWhen().end(), merged_events.get(i).getWhen().start(), false));
        }
      }
      }
      for (TimeRange time : potential) {
        if(time.duration() != 0 && time.duration() >= request.getDuration()) {
            finalized.add(time);
        }
      }
      return finalized;
  }
}
