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
import java.util.Hashtable;
import java.util.Enumeration;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      Collection<String> attendees = request.getAttendees();
      Collection<String> optional = request.getOptionalAttendees();
      long duration = request.getDuration();
      //EDGE CASES
      //return whole day if the meeting to schedule requires no attendees
      if (attendees.size() == 0 && optional.size() == 0) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      //return no availability if the meeting to schedule is longer than the day
      if (duration > 24*60) {
          return Arrays.asList();
      }
      //return whole day if there are no events at all that day
      if (events.size() == 0) {
          return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      //END EDGE CASES
      //otherwise, use algo

      ArrayList<Event> conflicting_events = new ArrayList<>();
      //retrieve all the events of attendees from the request
      for (Event event : events) {
        ArrayList<String> modifier = new ArrayList<String>(event.getAttendees());
        modifier.retainAll(request.getAttendees());
        if (modifier.size() >= 1) {
          conflicting_events.add(event);
        }
      }
      Collections.sort(conflicting_events);
      ArrayList<Event> merged = mergeEvents(conflicting_events);
      if (optional.size() > 0) {
          int slots = getAvailableTimes(conflicting_events, request).size();
          for (Enumeration optionals = optionalPeople(events, optional); optionals.hasMoreElements() ;) {
              ArrayList<String> name = new ArrayList<>();
              name.add((String)optionals.nextElement());
              ArrayList<Event> temporary = new ArrayList<>();
              for (Event event : conflicting_events) {
                  temporary.add(event);
              }
              for (Event event : events) {
                  ArrayList<String> modifier = new ArrayList<String>(event.getAttendees());
                  modifier.retainAll(name);
                  if (modifier.size() >= 1) {
                    temporary.add(event);
                  }
              }
              merged = mergeEvents(temporary);
              Collections.sort(merged);
              if (getAvailableTimes(merged, request).size() != 0) {
                conflicting_events = merged;
              }
          }
      }
      if (conflicting_events.size() == 0) {
          if (optional.size() == 0)
            return Arrays.asList(TimeRange.WHOLE_DAY);
          else
            return Arrays.asList();
      }
      return getAvailableTimes(conflicting_events, request);
  }

  public Enumeration optionalPeople(Collection<Event> events, Collection<String> optional) {
      Hashtable<String, Integer> dict = new Hashtable<String, Integer>();
      for(String person : optional) {
          dict.put(person, 0);
      }
      for(Event event : events) {
          ArrayList<String> modifier = new ArrayList<String>(event.getAttendees());
          modifier.retainAll(optional); //modifier at this point is a list of the people that are optional that have this event
          for (String person : modifier) {
              Integer count = dict.get(person);
              dict.put(person, count+event.getWhen().duration());
          }
      }
      return dict.keys();
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
          conflicting_events.remove(event) ;
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
